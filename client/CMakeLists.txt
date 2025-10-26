cmake_minimum_required(VERSION 3.20)
project(DonCEyKongJr_Client C)

set(CMAKE_C_STANDARD 99)

include_directories(${CMAKE_SOURCE_DIR}/include)

add_executable(client
    src/main.c
    src/network.c
)

if(WIN32)
    target_link_libraries(client PRIVATE ws2_32)
endif()