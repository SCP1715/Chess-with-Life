#include <jni.h>

#include <atomic>
#include <cstdint>
#include <deque>
#include <mutex>
#include <sstream>
#include <string>

#include "bitboard.h"
#include "endgame.h"
#include "evaluate.h"
#include "misc.h"
#include "piece.h"
#include "position.h"
#include "psqt.h"
#include "search.h"
#include "syzygy/tbprobe.h"
#include "thread.h"
#include "tune.h"
#include "tt.h"
#include "uci.h"
#include "variant.h"
#include "lifechess_draw.h"

namespace {

using namespace Stockfish;

std::once_flag engineInitFlag;
std::mutex engineMutex;
std::atomic<jlong> activeRequestId{0};
std::atomic<jlong> cancelRequestId{0};

struct ActiveRequestGuard {
    explicit ActiveRequestGuard(jlong value) : id(value) {}
    ~ActiveRequestGuard() {
        jlong expected = id;
        activeRequestId.compare_exchange_strong(expected, 0, std::memory_order_acq_rel);
    }
    jlong id;
};

void initialize_engine() {
    pieceMap.init();
    variants.init();
    UCI::init(Options);
    Tune::init();
    PSQT::init(variants.find("chess")->second);
    Bitboards::init();
    Position::init();
    Bitbases::init();
    Endgames::init();

    Options["Use NNUE"] = std::string("false");
    Options["UCI_Variant"] = std::string("lifechess");
    Options["Threads"] = std::string("1");
    Options["Hash"] = std::string("16");
    Search::clear();
}

std::string from_java(JNIEnv* env, jstring value) {
    if (value == nullptr)
        return {};
    const char* chars = env->GetStringUTFChars(value, nullptr);
    if (chars == nullptr)
        return {};
    std::string result(chars);
    env->ReleaseStringUTFChars(value, chars);
    return result;
}

jstring to_java(JNIEnv* env, const std::string& value) {
    return env->NewStringUTF(value.c_str());
}

bool load_position(const std::string& fen, const std::string& moveList,
                   Stockfish::Position& position,
                   Stockfish::StateListPtr& states) {
    const auto variant = Stockfish::variants.find("lifechess");
    if (variant == Stockfish::variants.end())
        return false;

    states.reset(new std::deque<Stockfish::StateInfo>(1));
    position.set(variant->second, fen, Stockfish::Options["UCI_Chess960"],
                 &states->back(), Stockfish::Threads.main());

    std::istringstream moves(moveList);
    std::string token;
    while (moves >> token) {
        Stockfish::Move move = Stockfish::UCI::to_move(position, token);
        if (move == Stockfish::MOVE_NONE || !position.legal(move))
            return false;
        states->emplace_back();
        position.do_move(move, states->back());
    }

    // Salt every historical key identically: repetition equality is preserved,
    // while TT entries from a different game/history cannot alias this search.
    std::uint64_t historyKey = 1469598103934665603ULL;
    const std::string context = fen + "\n" + moveList;
    for (unsigned char byte : context) {
        historyKey ^= byte;
        historyKey *= 1099511628211ULL;
    }
    if (historyKey == 0) historyKey = 1;
    for (auto& state : *states)
        state.key ^= static_cast<Stockfish::Key>(historyKey);
    return true;
}

std::string error_result(const char* message) {
    return std::string("ERROR|") + message;
}

} // namespace

extern "C" JNIEXPORT jstring JNICALL
Java_ru_lifeschess_engine_NativeStockfish_nativeEngineVersion(JNIEnv* env, jclass) {
    return to_java(env, Stockfish::engine_info());
}

extern "C" JNIEXPORT jstring JNICALL
Java_ru_lifeschess_engine_NativeStockfish_nativeLegalMoves(JNIEnv* env, jclass,
                                                           jstring fenValue) {
    std::call_once(engineInitFlag, initialize_engine);
    std::lock_guard<std::mutex> lock(engineMutex);

    Stockfish::Position position;
    Stockfish::StateListPtr states;
    if (!load_position(from_java(env, fenValue), {}, position, states))
        return to_java(env, error_result("variant unavailable"));

    std::ostringstream result;
    for (const auto& move : Stockfish::MoveList<Stockfish::LEGAL>(position))
        result << Stockfish::UCI::move(position, move) << '\n';
    return to_java(env, result.str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_ru_lifeschess_engine_NativeStockfish_nativeSearch(JNIEnv* env, jclass,
                                                        jlong requestId,
                                                        jlong stateRevision,
                                                        jstring gameIdValue,
                                                        jstring fenValue, jstring movesValue,
                                                        jint nodeBudget,
                                                        jboolean opponentOffered,
                                                        jint claimMask,
                                                        jboolean offerAlreadySent,
                                                        jboolean analysisMode) {
    if (nodeBudget < 1 || nodeBudget > 1'000'000)
        return to_java(env, error_result("node budget out of range"));

    std::call_once(engineInitFlag, initialize_engine);
    std::lock_guard<std::mutex> lock(engineMutex);
    activeRequestId.store(requestId, std::memory_order_release);
    ActiveRequestGuard requestGuard(requestId);
    if (cancelRequestId.load(std::memory_order_acquire) == requestId)
        return to_java(env, "CANCELLED||0");

    Stockfish::Position position;
    Stockfish::StateListPtr states;
    const std::string gameId = from_java(env, gameIdValue);
    const auto withRequestMetadata = [&](const std::string& value) {
        return value + "|gameId=" + gameId + "|requestId=" + std::to_string(requestId)
             + "|stateRevision=" + std::to_string(stateRevision);
    };
    if (!load_position(from_java(env, fenValue), from_java(env, movesValue), position, states))
        return to_java(env, error_result("invalid search root or historical move"));

    if (position.lifechess_bare_kings_claimable() && analysisMode != JNI_TRUE
        && opponentOffered != JNI_TRUE)
        return to_java(env, withRequestMetadata(
            "claim_bare_kings||0|reason=bare_kings|scoreType=exact|perspective=side_to_move|threshold=exact"));

    Stockfish::Search::LimitsType limits;
    limits.startTime = Stockfish::now();
    limits.nodes = static_cast<std::uint64_t>(nodeBudget);
    Stockfish::Search::clear();
    Stockfish::Threads.start_thinking(position, states, limits);
    // The pool-level wait intentionally excludes the main thread: normal UCI
    // calls run that search synchronously. JNI starts it on the engine thread,
    // so wait for it explicitly before reading depth, score, and best move.
    Stockfish::Threads.main()->wait_for_search_finished();

    if (cancelRequestId.load(std::memory_order_acquire) == requestId) {
        activeRequestId.store(0, std::memory_order_release);
        return to_java(env, "CANCELLED||0");
    }

    const auto* bestThread = Stockfish::Threads.main();
    if (bestThread->rootMoves.empty() || bestThread->rootMoves[0].pv.empty())
        return to_java(env, "NO_MOVE||0");

    if (bestThread->completedDepth <= 0) {
        const auto nodes = Stockfish::Threads.nodes_searched();
        const bool stopped = Stockfish::Threads.stop.load(std::memory_order_relaxed);
        const bool aborted = Stockfish::Threads.abort.load(std::memory_order_relaxed);
        return to_java(env, "ERROR|search did not complete an iteration; nodes="
            + std::to_string(nodes) + ", budget=" + std::to_string(nodeBudget)
            + ", stop=" + std::to_string(stopped)
            + ", abort=" + std::to_string(aborted));
    }
    const Stockfish::Value score = bestThread->rootMoves[0].score;
    if (score <= -Stockfish::VALUE_INFINITE || score >= Stockfish::VALUE_INFINITE)
        return to_java(env, error_result("search returned an incomplete score"));
    const bool repetitionClaim = (claimMask & 1) != 0;
    const bool fiftyMoveClaim = (claimMask & 2) != 0;
    const int materialPhase = std::min(24, 4 * position.count<Stockfish::QUEEN>()
            + 2 * position.count<Stockfish::ROOK>()
            + position.count<Stockfish::BISHOP>()
            + position.count<Stockfish::KNIGHT>());
    const auto offerThreshold = Stockfish::LifeChess::draw_score_threshold(materialPhase);
    const auto decision = Stockfish::LifeChess::draw_decision(
        score, opponentOffered == JNI_TRUE, repetitionClaim || fiftyMoveClaim,
        offerAlreadySent == JNI_TRUE, analysisMode == JNI_TRUE, offerThreshold,
        (claimMask & 4) != 0,
        Stockfish::LifeChess::draw_offer_is_stable(score, offerThreshold,
                                                    bestThread->completedDepth));
    const char* decisionName = "continue";
    const char* reason = "none";
    switch (decision) {
    case Stockfish::LifeChess::DrawDecision::Claim:
        decisionName = repetitionClaim ? "claim_repetition"
                     : fiftyMoveClaim ? "claim_fifty_moves" : "claim";
        reason = Stockfish::LifeChess::claim_reason(repetitionClaim, fiftyMoveClaim);
        break;
    case Stockfish::LifeChess::DrawDecision::Accept: decisionName = "accept"; reason = "opponent_offer"; break;
    case Stockfish::LifeChess::DrawDecision::Decline: decisionName = "decline"; reason = "winning_continuation"; break;
    case Stockfish::LifeChess::DrawDecision::Offer: decisionName = "offer"; reason = "score_threshold"; break;
    case Stockfish::LifeChess::DrawDecision::Continue: break;
    case Stockfish::LifeChess::DrawDecision::ClaimBareKings:
        decisionName = "claim_bare_kings"; reason = "bare_kings"; break;
    }

    activeRequestId.store(0, std::memory_order_release);
    const bool mateScore = score >= Stockfish::VALUE_MATE_IN_MAX_PLY
                        || score <= Stockfish::VALUE_MATED_IN_MAX_PLY;
    return to_java(env, withRequestMetadata(std::string(decisionName) + "|"
        + Stockfish::UCI::move(position, bestThread->rootMoves[0].pv[0]) + "|"
        + std::to_string(score) + "|reason=" + reason
        + (mateScore ? "|scoreType=mate" : "|scoreType=cp")
        + "|perspective=side_to_move|threshold=" + std::to_string(offerThreshold)
        + (score > Stockfish::VALUE_DRAW ? "|resetOfferLatch=true"
                                          : "|resetOfferLatch=false")));
}

extern "C" JNIEXPORT void JNICALL
Java_ru_lifeschess_engine_NativeStockfish_nativeCancel(JNIEnv*, jclass, jlong requestId) {
    cancelRequestId.store(requestId, std::memory_order_release);
    if (activeRequestId.load(std::memory_order_acquire) == requestId) {
        Stockfish::Threads.stop = true;
    }
}
