// Independent exhaustive KBKB Bellman/DTLK checker.
// Intentionally uses a separate board representation and move generator from
// solve.cpp. The Python checker remains the small readable reference oracle.
#include <algorithm>
#include <array>
#include <cstdint>
#include <fstream>
#include <iostream>
#include <limits>
#include <stdexcept>
#include <string>
#include <vector>

#pragma pack(push, 1)
struct FileHeader {
    char magic[8];
    std::uint32_t format;
    std::uint32_t rules;
    std::uint64_t entries;
    std::uint64_t valid;
    std::uint8_t kind, minor, color, reserved;
    char model[24];
};
struct Value { std::uint8_t wdl; std::uint32_t dtlk; };
#pragma pack(pop)

constexpr std::uint8_t WIN = 1, DRAW = 2, LOSS = 3, INVALID = 255;
constexpr std::uint8_t TABLE_KBKB = 3, TABLE_SINGLE_MINOR = 2;
constexpr std::uint8_t BISHOP = 1, WHITE = 1, BLACK = 2;
constexpr std::uint32_t NO_DISTANCE = std::numeric_limits<std::uint32_t>::max();

std::vector<Value> read_table(const std::string& path, std::uint8_t kind,
                              std::uint8_t minor = 0, std::uint8_t color = 0) {
    std::ifstream file(path, std::ios::binary);
    if (!file) throw std::runtime_error("cannot open " + path);
    FileHeader header{};
    file.read(reinterpret_cast<char*>(&header), sizeof(header));
    const char expected[8] = {'L','C','T','B','1','\0','\0','\0'};
    if (!file || !std::equal(expected, expected + 8, header.magic) ||
        header.format != 1 || header.rules != 100 || header.kind != kind ||
        header.minor != minor || header.color != color ||
        std::string(header.model, std::find(header.model, header.model + 24, '\0')) !=
            "MODEL_UNBOUNDED")
        throw std::runtime_error("invalid table header: " + path);
    if (header.entries > std::numeric_limits<std::size_t>::max() / sizeof(Value))
        throw std::runtime_error("table too large: " + path);
    std::vector<Value> result(static_cast<std::size_t>(header.entries));
    file.read(reinterpret_cast<char*>(result.data()),
              static_cast<std::streamsize>(result.size() * sizeof(Value)));
    if (!file || file.peek() != std::char_traits<char>::eof())
        throw std::runtime_error("truncated or trailing table data: " + path);
    return result;
}

std::uint32_t one_bishop_index(int wk, int bk, int bishop, int turn) {
    return static_cast<std::uint32_t>(((wk * 64 + bk) * 64 + bishop) * 2 + turn);
}

std::uint32_t kbkb_index(int wk, int bk, int wb, int bb, int turn) {
    return static_cast<std::uint32_t>((((wk * 64 + bk) * 64 + wb) * 64 + bb) * 2 + turn);
}

struct Checker {
    const std::vector<Value>& main;
    const std::vector<Value>& whiteBishop;
    const std::vector<Value>& blackBishop;
    std::uint64_t checked = 0, wins = 0, draws = 0, losses = 0, errors = 0;

    void examine(int wk, int bk, int wb, int bb, int turn) {
        const Value actual = main[kbkb_index(wk, bk, wb, bb, turn)];
        std::array<int, 64> board;
        board.fill(-1);
        board[wk] = 0; board[bk] = 1; board[wb] = 2; board[bb] = 3;
        bool immediate = false;
        bool hasLossChild = false;
        bool hasDrawChild = false;
        std::uint32_t fastestWin = NO_DISTANCE;
        std::uint32_t slowestLoss = 0;
        bool hasChild = false;

        auto consider = [&](int source, int target, int piece) {
            const int occupant = board[target];
            const int color = piece == 0 || piece == 2 ? WHITE : BLACK;
            if (occupant >= 0 && piece >= 2 &&
                ((occupant == 0 || occupant == 2) == (color == WHITE))) return;
            if (occupant == (turn == 0 ? 1 : 0)) {
                immediate = true;
                fastestWin = 1;
                return;
            }
            hasChild = true;
            Value child{};
            if (occupant >= 0) {
                int nextWk = wk, nextBk = bk, nextWb = wb, nextBb = bb;
                if (source == wk) nextWk = target;
                else if (source == bk) nextBk = target;
                else if (source == wb) nextWb = target;
                else nextBb = target;
                if (target == wb) nextWb = -1;
                if (target == bb) nextBb = -1;
                if ((nextWb >= 0) == (nextBb >= 0))
                    throw std::logic_error("capture did not reduce KBKB to KBK");
                if (nextWb >= 0)
                    child = whiteBishop[one_bishop_index(nextWk, nextBk, nextWb, 1 - turn)];
                else
                    child = blackBishop[one_bishop_index(nextWk, nextBk, nextBb, 1 - turn)];
            } else {
                int nextWk = wk, nextBk = bk, nextWb = wb, nextBb = bb;
                if (source == wk) nextWk = target;
                else if (source == bk) nextBk = target;
                else if (source == wb) nextWb = target;
                else nextBb = target;
                child = main[kbkb_index(nextWk, nextBk, nextWb, nextBb, 1 - turn)];
            }
            if (child.wdl == LOSS) {
                hasLossChild = true;
                fastestWin = std::min(fastestWin, child.dtlk + 1);
            } else if (child.wdl == DRAW) {
                hasDrawChild = true;
            } else if (child.wdl == WIN) {
                slowestLoss = std::max(slowestLoss, child.dtlk + 1);
            } else {
                throw std::logic_error("edge reaches invalid table entry");
            }
        };

        // Generate king moves by adjacent offsets, bishop moves by rays. Each
        // generator is local to this verifier and does not call solver code.
        for (int piece : {0, 1, 2, 3}) {
            const int color = piece == 0 || piece == 2 ? 0 : 1;
            if (color != turn) continue;
            const int source = piece == 0 ? wk : piece == 1 ? bk : piece == 2 ? wb : bb;
            const int row = source / 8, col = source % 8;
            if (piece < 2) {
                for (int dr = -1; dr <= 1; ++dr)
                    for (int dc = -1; dc <= 1; ++dc) {
                        if (!dr && !dc) continue;
                        const int r = row + dr, c = col + dc;
                        if (r >= 0 && r < 8 && c >= 0 && c < 8)
                            consider(source, r * 8 + c, piece);
                    }
            } else {
                for (int dr : {-1, 1})
                    for (int dc : {-1, 1}) {
                        int r = row + dr, c = col + dc;
                        while (r >= 0 && r < 8 && c >= 0 && c < 8) {
                            const int target = r * 8 + c;
                            consider(source, target, piece);
                            if (board[target] >= 0) break;
                            r += dr; c += dc;
                        }
                    }
            }
        }

        std::uint8_t expected;
        std::uint32_t expectedDistance;
        if (immediate || hasLossChild) {
            expected = WIN;
            expectedDistance = fastestWin;
        } else if (hasChild && !hasDrawChild) {
            expected = LOSS;
            expectedDistance = slowestLoss;
        } else {
            expected = DRAW;
            expectedDistance = 0;
        }
        ++checked;
        wins += actual.wdl == WIN;
        draws += actual.wdl == DRAW;
        losses += actual.wdl == LOSS;
        if (actual.wdl != expected || actual.dtlk != expectedDistance) {
            ++errors;
            if (errors <= 16)
                std::cerr << "MISMATCH wk=" << wk << " bk=" << bk << " wb=" << wb
                          << " bb=" << bb << " turn=" << turn << " actual="
                          << static_cast<int>(actual.wdl) << '/' << actual.dtlk
                          << " expected=" << static_cast<int>(expected) << '/'
                          << expectedDistance << '\n';
        }
    }
};

int main(int argc, char** argv) {
    try {
        if (argc != 2) throw std::runtime_error("usage: verify_kbkb <table-directory>");
        const std::string root = argv[1];
        const auto main = read_table(root + "/KBKB.lctb", TABLE_KBKB);
        const auto white = read_table(root + "/KBK-WHITE.lctb", TABLE_SINGLE_MINOR,
                                      BISHOP, WHITE);
        const auto black = read_table(root + "/KBK-BLACK.lctb", TABLE_SINGLE_MINOR,
                                      BISHOP, BLACK);
        Checker checker{main, white, black};
        for (int wk = 0; wk < 64; ++wk)
            for (int bk = 0; bk < 64; ++bk) {
                if (bk == wk) continue;
                for (int wb = 0; wb < 64; ++wb) {
                    if (wb == wk || wb == bk) continue;
                    for (int bb = 0; bb < 64; ++bb) {
                        if (bb == wk || bb == bk || bb == wb) continue;
                        checker.examine(wk, bk, wb, bb, 0);
                        checker.examine(wk, bk, wb, bb, 1);
                    }
                }
                if (wk == 0 && (bk % 4) == 0)
                    std::cout << "progress checked=" << checker.checked << std::endl;
            }
        std::cout << "KBKB verified states=" << checker.checked << " W/D/L="
                  << checker.wins << '/' << checker.draws << '/' << checker.losses
                  << " recurrence_errors=" << checker.errors << std::endl;
        if (checker.checked != 30498048 || checker.errors != 0) return 1;
        return 0;
    } catch (const std::exception& error) {
        std::cerr << "ERROR: " << error.what() << '\n';
        return 2;
    }
}
