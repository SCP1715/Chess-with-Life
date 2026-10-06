cmake_minimum_required(VERSION 3.22.1)
project(lifechess_native LANGUAGES C CXX)

get_filename_component(REPOSITORY_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/../../../.." ABSOLUTE)
set(FAIRY_STOCKFISH_ROOT "${REPOSITORY_ROOT}/third_party/fairy-stockfish/src")

set(FAIRY_STOCKFISH_SOURCES
    benchmark.cpp
    bitbase.cpp
    bitboard.cpp
    endgame.cpp
    evaluate.cpp
    material.cpp
    misc.cpp
    movegen.cpp
    movepick.cpp
    pawns.cpp
    position.cpp
    psqt.cpp
    search.cpp
    thread.cpp
    timeman.cpp
    tt.cpp
    uci.cpp
    ucioption.cpp
    tune.cpp
    syzygy/tbprobe.cpp
    nnue/evaluate_nnue.cpp
    nnue/features/half_ka_v2.cpp
    partner.cpp
    parser.cpp
    piece.cpp
    variant.cpp
    xboard.cpp
    nnue/features/half_ka_v2_variants.cpp
)

list(TRANSFORM FAIRY_STOCKFISH_SOURCES PREPEND "${FAIRY_STOCKFISH_ROOT}/")

add_library(lifechess SHARED
    lifechess_jni.cpp
    ${FAIRY_STOCKFISH_SOURCES}
)

target_include_directories(lifechess PRIVATE "${FAIRY_STOCKFISH_ROOT}")
target_compile_features(lifechess PRIVATE cxx_std_17)
target_compile_definitions(lifechess PRIVATE NNUE_EMBEDDING_OFF)
target_compile_options(lifechess PRIVATE -Wall -Wextra -Wno-unused-parameter)

# Keep 16 KiB page-size compatibility with NDK r25c's LLD linker.
target_link_options(lifechess PRIVATE
    "-Wl,-z,max-page-size=16384"
    "-Wl,-z,common-page-size=16384"
)
