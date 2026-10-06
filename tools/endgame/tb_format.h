#pragma once

#include <cstdint>

namespace lifechess_tb {

constexpr char MAGIC[8] = {'L', 'C', 'T', 'B', '1', '\0', '\0', '\0'};
constexpr std::uint32_t FORMAT_VERSION = 1;
constexpr std::uint32_t RULES_VERSION = 100; // docs/lifechess-rules.md 1.0
constexpr std::uint8_t TABLE_KK = 1;
constexpr std::uint8_t TABLE_SINGLE_MINOR = 2;
constexpr std::uint8_t TABLE_KBKB = 3;
constexpr std::uint8_t MINOR_BISHOP = 1;
constexpr std::uint8_t MINOR_KNIGHT = 2;
constexpr std::uint8_t MINOR_ROOK = 3;
constexpr std::uint8_t MINOR_QUEEN = 4;
constexpr std::uint8_t COLOR_NONE = 0;
constexpr std::uint8_t COLOR_WHITE = 1;
constexpr std::uint8_t COLOR_BLACK = 2;
constexpr std::uint8_t WDL_UNKNOWN = 0;
constexpr std::uint8_t WDL_WIN = 1;
constexpr std::uint8_t WDL_DRAW = 2;
constexpr std::uint8_t WDL_LOSS = 3;
constexpr std::uint8_t WDL_INVALID = 255;

#pragma pack(push, 1)
struct Header {
    char magic[8];
    std::uint32_t formatVersion;
    std::uint32_t rulesVersion;
    std::uint64_t indexSpace;
    std::uint64_t validStates;
    std::uint8_t tableKind;
    std::uint8_t minorKind;
    std::uint8_t minorColor;
    std::uint8_t reserved;
    char model[24];
};

struct Entry {
    std::uint8_t wdl;
    std::uint32_t dtlk;
};
#pragma pack(pop)

} // namespace lifechess_tb
