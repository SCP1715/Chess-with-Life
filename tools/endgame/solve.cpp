#include "tb_format.h"

#include <algorithm>
#include <array>
#include <cstdint>
#include <filesystem>
#include <fstream>
#include <iostream>
#include <limits>
#include <queue>
#include <stdexcept>
#include <string>
#include <tuple>
#include <vector>

using namespace lifechess_tb;
namespace fs = std::filesystem;
constexpr std::uint32_t INF = std::numeric_limits<std::uint32_t>::max();
constexpr std::uint32_t DTLK_PENDING = 1u << 31;

struct Node {
    std::uint8_t wk, bk, minor, turn;
};

struct KbkbNode {
    std::uint8_t wk, bk, wb, bb, turn;
};

struct MoveEdge {
    std::uint32_t parent;
    std::uint32_t child;
};

std::uint32_t kk_id(int wk, int bk, int turn) {
    return static_cast<std::uint32_t>((wk * 64 + bk) * 2 + turn);
}

std::uint32_t minor_id(int wk, int bk, int man, int turn) {
    return static_cast<std::uint32_t>(((wk * 64 + bk) * 64 + man) * 2 + turn);
}

std::uint32_t kbkb_id(int wk, int bk, int wb, int bb, int turn) {
    return static_cast<std::uint32_t>((((wk * 64 + bk) * 64 + wb) * 64 + bb) * 2 + turn);
}

bool inside(int r, int c) { return r >= 0 && r < 8 && c >= 0 && c < 8; }

bool clear_path(const std::array<int, 64>& board, int from, int to) {
    int r = from / 8, c = from % 8, tr = to / 8, tc = to % 8;
    int sr = (tr > r) - (tr < r), sc = (tc > c) - (tc < c);
    r += sr;
    c += sc;
    while (r != tr || c != tc) {
        if (board[r * 8 + c] != -1) return false;
        r += sr;
        c += sc;
    }
    return true;
}

bool attacks(const std::array<int, 64>& board, int from, int to, int type) {
    int dr = to / 8 - from / 8, dc = to % 8 - from % 8;
    int ar = std::abs(dr), ac = std::abs(dc);
    if (type == 0) return std::max(ar, ac) == 1; // king
    if (type == MINOR_KNIGHT) return ar * ac == 2;
    if (type == MINOR_BISHOP) return ar == ac && ar != 0 && clear_path(board, from, to);
    if (type == MINOR_ROOK) return ((dr == 0) != (dc == 0)) && clear_path(board, from, to);
    if (type == MINOR_QUEEN)
        return (dr == 0 || dc == 0 || ar == ac) && (dr != 0 || dc != 0) &&
               clear_path(board, from, to);
    return false;
}

struct EdgeSummary {
    std::uint32_t internal = 0;
    std::uint32_t externalDraw = 0;
    std::uint32_t externalWin = 0;
    std::uint32_t externalLoss = 0;
    std::uint32_t immediateWins = 0;
};

template<class Internal, class External, class Immediate>
EdgeSummary generate(const Node& n, bool hasMinor, int minorType, int minorColor,
                     Internal internal, External external, Immediate immediate) {
    std::array<int, 64> board;
    board.fill(-1);
    board[n.wk] = 0; // white king
    board[n.bk] = 1; // black king
    if (hasMinor) board[n.minor] = minorColor == COLOR_WHITE ? 2 : 3;
    const int ownKing = n.turn == 0 ? n.wk : n.bk;
    const int ownCode = n.turn == 0 ? 0 : 1;
    const int enemyKing = n.turn == 0 ? n.bk : n.wk;
    EdgeSummary summary;

    for (int from = 0; from < 64; ++from) {
        const int piece = board[from];
        if (piece < 0) continue;
        const bool king = piece < 2;
        const int color = (piece == 0 || piece == 2) ? COLOR_WHITE : COLOR_BLACK;
        if (color != (n.turn == 0 ? COLOR_WHITE : COLOR_BLACK)) continue;
        const int type = king ? 0 : minorType;
        for (int to = 0; to < 64; ++to) {
            if (to == from) continue;
            const int target = board[to];
            if (target >= 0 && !king && ((target == 0 || target == 2) == (color == COLOR_WHITE)))
                continue;
            if (!attacks(board, from, to, type)) continue;

            if (to == enemyKing) {
                ++summary.immediateWins;
                immediate();
                continue;
            }
            if (target >= 0) {
                // With two kings and one minor, any non-king capture reduces to KK.
                if (!hasMinor || !king || (target != 2 && target != 3))
                    throw std::logic_error("unexpected occupied-target transition");
                int wk = n.wk, bk = n.bk;
                if (from == wk) wk = to;
                else if (from == bk) bk = to;
                const std::uint8_t childWdl = external(wk, bk, 1 - n.turn);
                if (childWdl == WDL_WIN) ++summary.externalWin;
                else if (childWdl == WDL_LOSS) ++summary.externalLoss;
                else if (childWdl == WDL_DRAW) ++summary.externalDraw;
                else throw std::logic_error("incomplete KK dependency table");
                continue;
            }

            Node child = n;
            if (from == n.wk) child.wk = static_cast<std::uint8_t>(to);
            else if (from == n.bk) child.bk = static_cast<std::uint8_t>(to);
            else child.minor = static_cast<std::uint8_t>(to);
            child.turn = static_cast<std::uint8_t>(1 - n.turn);
            internal(child);
            ++summary.internal;
        }
    }
    (void)ownKing;
    (void)ownCode;
    return summary;
}

struct QueueItem {
    std::uint32_t distance;
    std::uint32_t id;
    bool operator>(const QueueItem& other) const {
        return std::tie(distance, id) > std::tie(other.distance, other.id);
    }
};

std::vector<Entry> solve_table(bool hasMinor, int minorType, int minorColor,
                               const std::vector<Entry>& kk,
                               std::uint64_t& validCount) {
    const std::uint32_t space = hasMinor ? 64u * 64u * 64u * 2u : 64u * 64u * 2u;
    std::vector<Entry> table(space, Entry{WDL_INVALID, 0});
    std::vector<std::uint32_t> indegree(space, 0);
    std::vector<Node> nodes;
    nodes.reserve(hasMinor ? 500000 : 8100);

    for (int wk = 0; wk < 64; ++wk)
        for (int bk = 0; bk < 64; ++bk) {
            if (wk == bk) continue;
            for (int man = 0; man < (hasMinor ? 64 : 1); ++man) {
                if (hasMinor && (man == wk || man == bk)) continue;
                for (int turn = 0; turn < 2; ++turn) {
                    Node n{static_cast<std::uint8_t>(wk), static_cast<std::uint8_t>(bk),
                           static_cast<std::uint8_t>(man), static_cast<std::uint8_t>(turn)};
                    const auto id = hasMinor ? minor_id(wk, bk, man, turn) : kk_id(wk, bk, turn);
                    table[id] = Entry{WDL_UNKNOWN, 0};
                    nodes.push_back(n);
                }
            }
        }
    validCount = nodes.size();

    std::vector<std::uint8_t> startsAsWin(space, 0);
    for (const Node& n : nodes) {
        const auto id = hasMinor ? minor_id(n.wk, n.bk, n.minor, n.turn) : kk_id(n.wk, n.bk, n.turn);
        const EdgeSummary s = generate(n, hasMinor, minorType, minorColor,
            [&](const Node& child) {
                const auto ci = hasMinor ? minor_id(child.wk, child.bk, child.minor, child.turn)
                                         : kk_id(child.wk, child.bk, child.turn);
                ++indegree[ci];
            },
            [&](int wk, int bk, int turn) -> std::uint8_t {
                return kk[kk_id(wk, bk, turn)].wdl;
            },
            [&]() { startsAsWin[id] = 1; });
        if (s.externalLoss != 0) startsAsWin[id] = 1;
    }

    std::vector<std::uint64_t> offsets(space + 1, 0);
    for (std::uint32_t i = 0; i < space; ++i) offsets[i + 1] = offsets[i] + indegree[i];
    std::vector<std::uint32_t> parents(static_cast<std::size_t>(offsets.back()));
    std::vector<std::uint64_t> cursor(offsets.begin(), offsets.end() - 1);
    std::vector<std::uint32_t> starts(space, 0), rem(space, 0);
    for (const Node& n : nodes) {
        const auto id = hasMinor ? minor_id(n.wk, n.bk, n.minor, n.turn) : kk_id(n.wk, n.bk, n.turn);
        const EdgeSummary s = generate(n, hasMinor, minorType, minorColor,
            [&](const Node& child) {
                const auto ci = hasMinor ? minor_id(child.wk, child.bk, child.minor, child.turn)
                                         : kk_id(child.wk, child.bk, child.turn);
                parents[cursor[ci]++] = id;
            },
            [&](int wk, int bk, int turn) -> std::uint8_t {
                return kk[kk_id(wk, bk, turn)].wdl;
            },
            [&]() {});
        starts[id] = startsAsWin[id];
        // A fixed KK WIN child is already a completed losing choice for the
        // mover; a fixed KK LOSS child makes this state a WIN. Draw children
        // remain unresolved blockers for a LOSS proof.
        if (starts[id]) table[id].wdl = WDL_WIN;
        rem[id] = s.internal + s.externalDraw;
        if (!starts[id] && rem[id] == 0) table[id].wdl = WDL_LOSS;
    }

    std::queue<std::uint32_t> q;
    for (const Node& n : nodes) {
        auto id = hasMinor ? minor_id(n.wk, n.bk, n.minor, n.turn) : kk_id(n.wk, n.bk, n.turn);
        if (table[id].wdl == WDL_WIN || table[id].wdl == WDL_LOSS) q.push(id);
    }
    while (!q.empty()) {
        const auto child = q.front(); q.pop();
        for (std::uint64_t e = offsets[child]; e < offsets[child + 1]; ++e) {
            const auto parent = parents[static_cast<std::size_t>(e)];
            if (table[parent].wdl != WDL_UNKNOWN) continue;
            if (table[child].wdl == WDL_LOSS) {
                table[parent].wdl = WDL_WIN;
                q.push(parent);
            } else if (table[child].wdl == WDL_WIN) {
                if (rem[parent] == 0) throw std::logic_error("retrograde counter underflow");
                if (--rem[parent] == 0) {
                    table[parent].wdl = WDL_LOSS;
                    q.push(parent);
                }
            }
        }
    }
    for (const Node& n : nodes) {
        const auto id = hasMinor ? minor_id(n.wk, n.bk, n.minor, n.turn) : kk_id(n.wk, n.bk, n.turn);
        if (table[id].wdl == WDL_UNKNOWN) table[id].wdl = WDL_DRAW;
    }

    // Solve secondary remoteness only on the proved WIN/LOSS attractor.
    std::vector<std::uint32_t> distanceRemaining(space, 0), lossMax(space, 0);
    std::vector<std::uint8_t> finalized(space, 0);
    std::priority_queue<QueueItem, std::vector<QueueItem>, std::greater<QueueItem>> pq;
    for (const Node& n : nodes) {
        const auto id = hasMinor ? minor_id(n.wk, n.bk, n.minor, n.turn) : kk_id(n.wk, n.bk, n.turn);
        if (table[id].wdl != WDL_DRAW) table[id].dtlk = INF;
    }
    for (const Node& n : nodes) {
        const auto id = hasMinor ? minor_id(n.wk, n.bk, n.minor, n.turn) : kk_id(n.wk, n.bk, n.turn);
        if (table[id].wdl == WDL_WIN) {
            std::uint32_t best = INF;
            generate(n, hasMinor, minorType, minorColor,
                [&](const Node& child) {
                    const auto ci = hasMinor ? minor_id(child.wk, child.bk, child.minor, child.turn)
                                             : kk_id(child.wk, child.bk, child.turn);
                    if (table[ci].wdl == WDL_LOSS && table[ci].dtlk != INF)
                        best = std::min(best, table[ci].dtlk + 1);
                },
                [&](int wk, int bk, int turn) -> std::uint8_t {
                    const auto& child = kk[kk_id(wk, bk, turn)];
                    if (child.wdl == WDL_LOSS) best = std::min(best, child.dtlk + 1);
                    return child.wdl;
                },
                [&]() { best = 1; });
            if (best != INF) {
                table[id].dtlk = best;
                pq.push({best, id});
            }
        } else if (table[id].wdl == WDL_LOSS) {
            generate(n, hasMinor, minorType, minorColor,
                [&](const Node& child) {
                    const auto ci = hasMinor ? minor_id(child.wk, child.bk, child.minor, child.turn)
                                             : kk_id(child.wk, child.bk, child.turn);
                    if (table[ci].wdl != WDL_WIN) throw std::logic_error("LOSS has non-WIN child");
                    ++distanceRemaining[id];
                },
                [&](int wk, int bk, int turn) -> std::uint8_t {
                    const auto& child = kk[kk_id(wk, bk, turn)];
                    if (child.wdl != WDL_WIN) throw std::logic_error("LOSS has non-WIN KK child");
                    lossMax[id] = std::max(lossMax[id], child.dtlk);
                    return child.wdl;
                },
                [&]() { throw std::logic_error("LOSS has immediate win"); });
            if (distanceRemaining[id] == 0) {
                table[id].dtlk = lossMax[id] + 1;
                pq.push({table[id].dtlk, id});
            }
        }
    }
    while (!pq.empty()) {
        const auto item = pq.top(); pq.pop();
        if (finalized[item.id] || table[item.id].dtlk != item.distance) continue;
        finalized[item.id] = 1;
        for (std::uint64_t e = offsets[item.id]; e < offsets[item.id + 1]; ++e) {
            const auto parent = parents[static_cast<std::size_t>(e)];
            if (table[parent].wdl == WDL_WIN && table[item.id].wdl == WDL_LOSS) {
                const auto candidate = item.distance + 1;
                if (candidate < table[parent].dtlk) {
                    if (finalized[parent]) throw std::logic_error("DTLK WIN distance decreased after finalization");
                    table[parent].dtlk = candidate;
                    pq.push({candidate, parent});
                }
            } else if (table[parent].wdl == WDL_LOSS && table[item.id].wdl == WDL_WIN) {
                if (distanceRemaining[parent] == 0) throw std::logic_error("DTLK LOSS counter underflow");
                lossMax[parent] = std::max(lossMax[parent], item.distance);
                if (--distanceRemaining[parent] == 0) {
                    table[parent].dtlk = lossMax[parent] + 1;
                    pq.push({table[parent].dtlk, parent});
                }
            }
        }
    }
    for (const Node& n : nodes) {
        const auto id = hasMinor ? minor_id(n.wk, n.bk, n.minor, n.turn) : kk_id(n.wk, n.bk, n.turn);
        if (table[id].wdl != WDL_DRAW && !finalized[id])
            throw std::logic_error("unresolved DTLK in proved WIN/LOSS state");
        if (table[id].wdl == WDL_DRAW) table[id].dtlk = 0;
    }
    return table;
}

struct KbkbSummary {
    std::uint32_t internal = 0;
    std::uint32_t externalDraw = 0;
    bool externalLoss = false;
    bool immediateWin = false;
};

template<class Internal, class External, class Immediate>
KbkbSummary generate_kbkb(const KbkbNode& n, Internal internal, External external,
                          Immediate immediate) {
    std::array<int, 64> board;
    board.fill(-1);
    board[n.wk] = 0;
    board[n.bk] = 1;
    board[n.wb] = 2;
    board[n.bb] = 3;
    KbkbSummary summary;
    for (int from = 0; from < 64; ++from) {
        const int piece = board[from];
        if (piece < 0) continue;
        const int color = piece == 0 || piece == 2 ? COLOR_WHITE : COLOR_BLACK;
        if (color != (n.turn == 0 ? COLOR_WHITE : COLOR_BLACK)) continue;
        const bool king = piece < 2;
        const int type = king ? 0 : MINOR_BISHOP;
        for (int to = 0; to < 64; ++to) {
            if (to == from || !attacks(board, from, to, type)) continue;
            const int target = board[to];
            if (target >= 0 && !king &&
                ((target == 0 || target == 2) == (color == COLOR_WHITE))) continue;
            if (target == (n.turn == 0 ? 1 : 0)) {
                summary.immediateWin = true;
                immediate();
                continue;
            }
            if (target >= 0) {
                int wk = n.wk, bk = n.bk;
                int wb = n.wb, bb = n.bb;
                if (from == n.wk) wk = to;
                else if (from == n.bk) bk = to;
                else if (from == n.wb) wb = to;
                else bb = to;
                if (to == n.wb) wb = -1;
                if (to == n.bb) bb = -1;
                if ((wb >= 0) == (bb >= 0))
                    throw std::logic_error("KBKB capture did not leave exactly one bishop");
                const int minor = wb >= 0 ? wb : bb;
                const int minorColor = wb >= 0 ? COLOR_WHITE : COLOR_BLACK;
                const Entry child = external(wk, bk, minor, minorColor, 1 - n.turn);
                if (child.wdl == WDL_LOSS) summary.externalLoss = true;
                else if (child.wdl == WDL_DRAW) ++summary.externalDraw;
                else if (child.wdl != WDL_WIN)
                    throw std::logic_error("incomplete KBK dependency table");
                continue;
            }
            KbkbNode child = n;
            if (from == n.wk) child.wk = static_cast<std::uint8_t>(to);
            else if (from == n.bk) child.bk = static_cast<std::uint8_t>(to);
            else if (from == n.wb) child.wb = static_cast<std::uint8_t>(to);
            else child.bb = static_cast<std::uint8_t>(to);
            child.turn = static_cast<std::uint8_t>(1 - n.turn);
            internal(child);
            ++summary.internal;
        }
    }
    return summary;
}

template<class Parent>
void kbkb_predecessors(const KbkbNode& child, Parent parentCallback) {
    std::array<int, 64> board;
    board.fill(-1);
    board[child.wk] = 0;
    board[child.bk] = 1;
    board[child.wb] = 2;
    board[child.bb] = 3;
    const int movedSquare = child.turn == 1 ? child.wk : child.bk;
    const int movedBishop = child.turn == 1 ? child.wb : child.bb;
    auto emitKing = [&](int from, int to) {
        KbkbNode parent = child;
        if (movedSquare == child.wk) parent.wk = static_cast<std::uint8_t>(from);
        else parent.bk = static_cast<std::uint8_t>(from);
        parent.turn = static_cast<std::uint8_t>(1 - child.turn);
        parentCallback(parent);
        (void)to;
    };
    const int kingRow = movedSquare / 8, kingCol = movedSquare % 8;
    for (int dr = -1; dr <= 1; ++dr)
        for (int dc = -1; dc <= 1; ++dc) {
            if (dr == 0 && dc == 0) continue;
            const int row = kingRow + dr, col = kingCol + dc;
            if (inside(row, col)) {
                const int from = row * 8 + col;
                if (board[from] < 0) emitKing(from, movedSquare);
            }
        }

    const int bishopRow = movedBishop / 8, bishopCol = movedBishop % 8;
    for (int dr : {-1, 1})
        for (int dc : {-1, 1}) {
            int row = bishopRow + dr, col = bishopCol + dc;
            while (inside(row, col)) {
                const int from = row * 8 + col;
                if (board[from] >= 0) break;
                KbkbNode parent = child;
                if (movedBishop == child.wb) parent.wb = static_cast<std::uint8_t>(from);
                else parent.bb = static_cast<std::uint8_t>(from);
                parent.turn = static_cast<std::uint8_t>(1 - child.turn);
                parentCallback(parent);
                row += dr;
                col += dc;
            }
        }
}

std::vector<Entry> solve_kbkb(const std::vector<Entry>& kbkWhite,
                              const std::vector<Entry>& kbkBlack,
                              std::uint64_t& validCount) {
    constexpr std::uint32_t space = 64u * 64u * 64u * 64u * 2u;
    std::vector<Entry> table(space, Entry{WDL_INVALID, 0});
    std::vector<std::uint32_t> remaining(space, 0);
    std::uint64_t valid = 0;

    for (int wk = 0; wk < 64; ++wk)
        for (int bk = 0; bk < 64; ++bk) {
            if (wk == bk) continue;
            for (int wb = 0; wb < 64; ++wb) {
                if (wb == wk || wb == bk) continue;
                for (int bb = 0; bb < 64; ++bb) {
                    if (bb == wk || bb == bk || bb == wb) continue;
                    const auto base = kbkb_id(wk, bk, wb, bb, 0);
                    table[base].wdl = WDL_UNKNOWN;
                    table[base + 1].wdl = WDL_UNKNOWN;
                    valid += 2;
                }
            }
        }
    validCount = valid;

    for (std::uint32_t id = 0; id < space; ++id) {
        if (table[id].wdl == WDL_INVALID) continue;
        const std::uint32_t boardId = id / 2;
        KbkbNode n{static_cast<std::uint8_t>(boardId / (64 * 64 * 64)),
                   static_cast<std::uint8_t>(boardId / (64 * 64) % 64),
                   static_cast<std::uint8_t>(boardId / 64 % 64),
                   static_cast<std::uint8_t>(boardId % 64),
                   static_cast<std::uint8_t>(id % 2)};
        const auto s = generate_kbkb(n, [](const KbkbNode&) {},
            [&](int wk, int bk, int minor, int color, int turn) {
                const auto& dependency = color == COLOR_WHITE ? kbkWhite : kbkBlack;
                return dependency[minor_id(wk, bk, minor, turn)];
            }, []() {});
        if (s.immediateWin || s.externalLoss) table[id].wdl = WDL_WIN;
        remaining[id] = s.internal + s.externalDraw;
        if (table[id].wdl == WDL_UNKNOWN && remaining[id] == 0)
            table[id].wdl = WDL_LOSS;
    }

    std::vector<std::uint32_t> queue;
    queue.reserve(validCount / 3);
    for (std::uint32_t id = 0; id < space; ++id)
        if (table[id].wdl == WDL_WIN || table[id].wdl == WDL_LOSS)
            queue.push_back(id);
    for (std::size_t head = 0; head < queue.size(); ++head) {
        const auto childId = queue[head];
        const std::uint32_t boardId = childId / 2;
        const KbkbNode child{static_cast<std::uint8_t>(boardId / (64 * 64 * 64)),
                             static_cast<std::uint8_t>(boardId / (64 * 64) % 64),
                             static_cast<std::uint8_t>(boardId / 64 % 64),
                             static_cast<std::uint8_t>(boardId % 64),
                             static_cast<std::uint8_t>(childId % 2)};
        kbkb_predecessors(child, [&](const KbkbNode& parentNode) {
            const auto parent = kbkb_id(parentNode.wk, parentNode.bk,
                                        parentNode.wb, parentNode.bb, parentNode.turn);
            if (table[parent].wdl != WDL_UNKNOWN) return;
            if (table[childId].wdl == WDL_LOSS) {
                table[parent].wdl = WDL_WIN;
                queue.push_back(parent);
            } else {
                if (remaining[parent] == 0)
                    throw std::logic_error("KBKB retrograde counter underflow");
                if (--remaining[parent] == 0) {
                    table[parent].wdl = WDL_LOSS;
                    queue.push_back(parent);
                }
            }
        });
    }
    queue.clear();
    queue.shrink_to_fit();
    remaining.shrink_to_fit();
    for (auto& entry : table)
        if (entry.wdl == WDL_UNKNOWN) entry.wdl = WDL_DRAW;

    std::vector<std::uint32_t> pending(space, 0);
    std::vector<std::uint8_t> finalized(space, 0);
    std::priority_queue<QueueItem, std::vector<QueueItem>, std::greater<QueueItem>> pq;
    for (auto& entry : table)
        if (entry.wdl == WDL_WIN || entry.wdl == WDL_LOSS) entry.dtlk = INF;
    for (std::uint32_t id = 0; id < space; ++id) {
        if (table[id].wdl == WDL_DRAW || table[id].wdl == WDL_INVALID) continue;
        const std::uint32_t boardId = id / 2;
        const KbkbNode n{static_cast<std::uint8_t>(boardId / (64 * 64 * 64)),
                         static_cast<std::uint8_t>(boardId / (64 * 64) % 64),
                         static_cast<std::uint8_t>(boardId / 64 % 64),
                         static_cast<std::uint8_t>(boardId % 64),
                         static_cast<std::uint8_t>(id % 2)};
        if (table[id].wdl == WDL_WIN) {
            std::uint32_t best = INF;
            generate_kbkb(n, [&](const KbkbNode& child) {
                const auto ci = kbkb_id(child.wk, child.bk, child.wb, child.bb, child.turn);
                if (table[ci].wdl == WDL_LOSS && table[ci].dtlk != INF &&
                    (table[ci].dtlk & DTLK_PENDING) == 0)
                    best = std::min(best, table[ci].dtlk + 1);
            }, [&](int wk, int bk, int minor, int color, int turn) {
                const auto& dependency = color == COLOR_WHITE ? kbkWhite : kbkBlack;
                const auto& child = dependency[minor_id(wk, bk, minor, turn)];
                if (child.wdl == WDL_LOSS) best = std::min(best, child.dtlk + 1);
                return child;
            }, [&]() { best = 1; });
            if (best != INF) {
                table[id].dtlk = best;
                pq.push({best, id});
            }
        } else {
            std::uint32_t maxChild = 0;
            const auto summary = generate_kbkb(n, [&](const KbkbNode& child) {
                const auto ci = kbkb_id(child.wk, child.bk, child.wb, child.bb, child.turn);
                if (table[ci].wdl != WDL_WIN)
                    throw std::logic_error("KBKB LOSS has non-WIN internal child");
                ++pending[id];
            }, [&](int wk, int bk, int minor, int color, int turn) {
                const auto& dependency = color == COLOR_WHITE ? kbkWhite : kbkBlack;
                const auto& child = dependency[minor_id(wk, bk, minor, turn)];
                if (child.wdl != WDL_WIN)
                    throw std::logic_error("KBKB LOSS has non-WIN external child");
                maxChild = std::max(maxChild, child.dtlk);
                return child;
            }, [&]() { throw std::logic_error("KBKB LOSS has immediate win"); });
            (void)summary;
            // External winning children are already finalized; seed the max
            // with their largest distance. Internal children arrive by queue.
            generate_kbkb(n, [](const KbkbNode&) {}, [&](int wk, int bk, int minor,
                int color, int turn) {
                const auto& dependency = color == COLOR_WHITE ? kbkWhite : kbkBlack;
                const auto& child = dependency[minor_id(wk, bk, minor, turn)];
                if (child.wdl == WDL_WIN) maxChild = std::max(maxChild, child.dtlk);
                return child;
            }, []() {});
            if (pending[id] == 0) {
                table[id].dtlk = maxChild + 1;
                pq.push({table[id].dtlk, id});
            } else {
                table[id].dtlk = DTLK_PENDING | maxChild;
            }
        }
    }

    while (!pq.empty()) {
        const auto item = pq.top(); pq.pop();
        if (finalized[item.id] || table[item.id].dtlk != item.distance) continue;
        finalized[item.id] = 1;
        const auto boardId = item.id / 2;
        const KbkbNode child{static_cast<std::uint8_t>(boardId / (64 * 64 * 64)),
                             static_cast<std::uint8_t>(boardId / (64 * 64) % 64),
                             static_cast<std::uint8_t>(boardId / 64 % 64),
                             static_cast<std::uint8_t>(boardId % 64),
                             static_cast<std::uint8_t>(item.id % 2)};
        kbkb_predecessors(child, [&](const KbkbNode& parentNode) {
            const auto parent = kbkb_id(parentNode.wk, parentNode.bk,
                                        parentNode.wb, parentNode.bb, parentNode.turn);
            if (table[parent].wdl == WDL_WIN && table[item.id].wdl == WDL_LOSS) {
                const auto candidate = item.distance + 1;
                if (candidate < table[parent].dtlk) {
                    if (finalized[parent])
                        throw std::logic_error("KBKB WIN distance decreased after finalization");
                    table[parent].dtlk = candidate;
                    pq.push({candidate, parent});
                }
            } else if (table[parent].wdl == WDL_LOSS && table[item.id].wdl == WDL_WIN) {
                if (pending[parent] == 0)
                    throw std::logic_error("KBKB DTLK counter underflow");
                const std::uint32_t accumulated = table[parent].dtlk & ~DTLK_PENDING;
                table[parent].dtlk = DTLK_PENDING | std::max(accumulated, item.distance);
                if (--pending[parent] == 0) {
                    table[parent].dtlk = (table[parent].dtlk & ~DTLK_PENDING) + 1;
                    pq.push({table[parent].dtlk, parent});
                }
            }
        });
    }
    for (std::uint32_t id = 0; id < space; ++id) {
        if (table[id].wdl == WDL_DRAW) table[id].dtlk = 0;
        else if (table[id].wdl != WDL_INVALID && !finalized[id])
            throw std::logic_error("KBKB decisive state has unresolved DTLK");
    }
    return table;
}

void save_table(const fs::path& path, bool hasMinor, int minorType, int minorColor,
                std::uint64_t validStates, const std::vector<Entry>& entries) {
    Header header{};
    std::copy(std::begin(MAGIC), std::end(MAGIC), std::begin(header.magic));
    header.formatVersion = FORMAT_VERSION;
    header.rulesVersion = RULES_VERSION;
    header.indexSpace = entries.size();
    header.validStates = validStates;
    header.tableKind = hasMinor ? TABLE_SINGLE_MINOR : TABLE_KK;
    header.minorKind = static_cast<std::uint8_t>(minorType);
    header.minorColor = static_cast<std::uint8_t>(minorColor);
    std::string model = "MODEL_UNBOUNDED";
    std::copy(model.begin(), model.end(), header.model);
    fs::path temporary = path;
    temporary += ".tmp";
    if (fs::exists(temporary)) fs::remove(temporary);
    std::ofstream out(temporary, std::ios::binary | std::ios::trunc);
    if (!out) throw std::runtime_error("cannot open table for writing: " + temporary.string());
    out.write(reinterpret_cast<const char*>(&header), sizeof(header));
    out.write(reinterpret_cast<const char*>(entries.data()),
              static_cast<std::streamsize>(entries.size() * sizeof(Entry)));
    if (!out) throw std::runtime_error("failed while writing table: " + temporary.string());
    out.close();
    if (fs::exists(path)) throw std::runtime_error("refusing to overwrite completed table: " + path.string());
    fs::rename(temporary, path);
}

std::vector<Entry> load_table(const fs::path& path, bool hasMinor, int minorType,
                              int minorColor, std::uint64_t& validStates) {
    std::ifstream in(path, std::ios::binary);
    if (!in) throw std::runtime_error("cannot open existing table: " + path.string());
    Header header{};
    in.read(reinterpret_cast<char*>(&header), sizeof(header));
    const std::uint64_t expectedSpace = hasMinor ? 64ull * 64 * 64 * 2 : 64ull * 64 * 2;
    if (!in || !std::equal(std::begin(MAGIC), std::end(MAGIC), std::begin(header.magic)) ||
        header.formatVersion != FORMAT_VERSION || header.rulesVersion != RULES_VERSION ||
        header.indexSpace != expectedSpace ||
        header.tableKind != (hasMinor ? TABLE_SINGLE_MINOR : TABLE_KK) ||
        header.minorKind != static_cast<std::uint8_t>(minorType) ||
        header.minorColor != static_cast<std::uint8_t>(minorColor) ||
        std::string(std::begin(header.model),
                    std::find(std::begin(header.model), std::end(header.model), '\0')) !=
            "MODEL_UNBOUNDED")
        throw std::runtime_error("existing table header does not match requested class: " + path.string());
    std::vector<Entry> entries(static_cast<std::size_t>(expectedSpace));
    in.read(reinterpret_cast<char*>(entries.data()),
            static_cast<std::streamsize>(entries.size() * sizeof(Entry)));
    if (!in || in.peek() != std::char_traits<char>::eof())
        throw std::runtime_error("existing table has truncated or trailing data: " + path.string());
    validStates = header.validStates;
    return entries;
}

void save_kbkb_table(const fs::path& path, std::uint64_t validStates,
                     const std::vector<Entry>& entries) {
    Header header{};
    std::copy(std::begin(MAGIC), std::end(MAGIC), std::begin(header.magic));
    header.formatVersion = FORMAT_VERSION;
    header.rulesVersion = RULES_VERSION;
    header.indexSpace = entries.size();
    header.validStates = validStates;
    header.tableKind = TABLE_KBKB;
    std::string model = "MODEL_UNBOUNDED";
    std::copy(model.begin(), model.end(), header.model);
    fs::path temporary = path;
    temporary += ".tmp";
    if (fs::exists(temporary)) fs::remove(temporary);
    std::ofstream out(temporary, std::ios::binary | std::ios::trunc);
    if (!out) throw std::runtime_error("cannot open table for writing: " + temporary.string());
    out.write(reinterpret_cast<const char*>(&header), sizeof(header));
    out.write(reinterpret_cast<const char*>(entries.data()),
              static_cast<std::streamsize>(entries.size() * sizeof(Entry)));
    if (!out) throw std::runtime_error("failed while writing table: " + temporary.string());
    out.close();
    if (fs::exists(path)) throw std::runtime_error("refusing to overwrite completed table: " + path.string());
    fs::rename(temporary, path);
}

std::vector<Entry> load_kbkb_table(const fs::path& path, std::uint64_t& validStates) {
    constexpr std::uint64_t expectedSpace = 64ull * 64 * 64 * 64 * 2;
    std::ifstream in(path, std::ios::binary);
    if (!in) throw std::runtime_error("cannot open existing table: " + path.string());
    Header header{};
    in.read(reinterpret_cast<char*>(&header), sizeof(header));
    if (!in || !std::equal(std::begin(MAGIC), std::end(MAGIC), std::begin(header.magic)) ||
        header.formatVersion != FORMAT_VERSION || header.rulesVersion != RULES_VERSION ||
        header.indexSpace != expectedSpace || header.tableKind != TABLE_KBKB ||
        std::string(std::begin(header.model),
                    std::find(std::begin(header.model), std::end(header.model), '\0')) !=
            "MODEL_UNBOUNDED")
        throw std::runtime_error("existing KBKB table header is invalid: " + path.string());
    std::vector<Entry> entries(static_cast<std::size_t>(expectedSpace));
    in.read(reinterpret_cast<char*>(entries.data()),
            static_cast<std::streamsize>(entries.size() * sizeof(Entry)));
    if (!in || in.peek() != std::char_traits<char>::eof())
        throw std::runtime_error("existing KBKB table is truncated or has trailing data: " + path.string());
    validStates = header.validStates;
    return entries;
}

std::string table_name(int type, int color) {
    const std::string name = type == MINOR_BISHOP ? "KBK" :
        type == MINOR_KNIGHT ? "KNK" : type == MINOR_ROOK ? "KRK" : "KQK";
    return name + (color == COLOR_WHITE ? "-WHITE.lctb" : "-BLACK.lctb");
}

int main(int argc, char** argv) {
    try {
        if (argc < 2 || argc > 3) {
            std::cerr << "Usage: lifechess_tb_solve <output-directory> "
                         "[ALL|KK|KBK|KNK|KRK|KQK|KBK-WHITE|KBK-BLACK|KNK-WHITE|KNK-BLACK|KRK-WHITE|KRK-BLACK|KQK-WHITE|KQK-BLACK|KBKB]\n";
            return 2;
        }
        const fs::path output = argv[1];
        const std::string requested = argc == 3 ? argv[2] : "ALL";
        const std::vector<std::string> choices = {
            "ALL", "KK", "KBK", "KNK", "KRK", "KQK",
            "KBK-WHITE", "KBK-BLACK", "KNK-WHITE", "KNK-BLACK",
            "KRK-WHITE", "KRK-BLACK", "KQK-WHITE", "KQK-BLACK", "KBKB"};
        if (std::find(choices.begin(), choices.end(), requested) == choices.end())
            throw std::invalid_argument("unknown table class: " + requested);
        fs::create_directories(output);
        std::uint64_t valid = 0;
        const fs::path kkPath = output / "KK.lctb";
        std::vector<Entry> kk;
        if (fs::exists(kkPath)) {
            kk = load_table(kkPath, false, 0, COLOR_NONE, valid);
            std::cout << "KK states=" << valid << " indexSpace=" << kk.size() << " loaded\n";
        } else {
            kk = solve_table(false, 0, COLOR_NONE, {}, valid);
            save_table(kkPath, false, 0, COLOR_NONE, valid, kk);
            std::cout << "KK states=" << valid << " indexSpace=" << kk.size() << " complete\n";
        }
        if (requested == "KK") return 0;
        for (int type : {MINOR_BISHOP, MINOR_KNIGHT, MINOR_ROOK, MINOR_QUEEN}) {
            for (int color : {COLOR_WHITE, COLOR_BLACK}) {
                const std::string family = type == MINOR_BISHOP ? "KBK" :
                    type == MINOR_KNIGHT ? "KNK" : type == MINOR_ROOK ? "KRK" : "KQK";
                const std::string name = family + (color == COLOR_WHITE ? "-WHITE" : "-BLACK");
                const bool selected = requested == "ALL" || requested == name ||
                    requested == family;
                if (!selected) continue;
                const fs::path path = output / table_name(type, color);
                if (fs::exists(path)) {
                    auto loaded = load_table(path, true, type, color, valid);
                    std::cout << name << " states=" << valid << " indexSpace="
                              << loaded.size() << " loaded\n";
                    continue;
                }
                auto table = solve_table(true, type, color, kk, valid);
                save_table(path, true, type, color, valid, table);
                std::cout << name << " states=" << valid << " indexSpace="
                          << table.size() << " complete\n";
            }
        }
        if (requested == "KBKB" || requested == "ALL") {
            const fs::path kbkbPath = output / "KBKB.lctb";
            if (fs::exists(kbkbPath)) {
                auto loaded = load_kbkb_table(kbkbPath, valid);
                std::cout << "KBKB states=" << valid << " indexSpace="
                          << loaded.size() << " loaded\n";
            } else {
                std::uint64_t whiteValid = 0, blackValid = 0;
                auto white = load_table(output / "KBK-WHITE.lctb", true,
                                        MINOR_BISHOP, COLOR_WHITE, whiteValid);
                auto black = load_table(output / "KBK-BLACK.lctb", true,
                                        MINOR_BISHOP, COLOR_BLACK, blackValid);
                if (whiteValid != blackValid)
                    throw std::runtime_error("KBK dependency tables disagree on state count");
                auto table = solve_kbkb(white, black, valid);
                save_kbkb_table(kbkbPath, valid, table);
                std::cout << "KBKB states=" << valid << " indexSpace="
                          << table.size() << " complete\n";
            }
        }
        return 0;
    } catch (const std::exception& e) {
        std::cerr << "ERROR: " << e.what() << '\n';
        return 1;
    }
}
