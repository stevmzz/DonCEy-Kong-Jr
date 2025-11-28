cmake_minimum_required(VERSION 3.20)
project(DonCEyKongJr_Client C)

set(CMAKE_C_STANDARD 99)

set(RAYLIB_PATH "C:/raylib")

# ESTO DEBE ESTAR PRIMERO
include_directories(${RAYLIB_PATH}/include)

# DESPUÉS LOS OTROS
include_directories(${CMAKE_SOURCE_DIR}/include)
include_directories(${CMAKE_SOURCE_DIR}/include/screen)

add_executable(client
    src/main.c
    src/network.c
    src/state_manager.c
    src/input_handler.c
    src/screen/main_screen.c
    src/screen/player_screen.c
    src/screen/spectator_screen.c
)

if(WIN32)
    target_link_libraries(client PRIVATE 
        ws2_32
        ${RAYLIB_PATH}/lib/raylib.lib
        winmm
        gdi32
    )
    
    if(EXISTS "${RAYLIB_PATH}/lib/raylib.dll")
        add_custom_command(TARGET client POST_BUILD
            COMMAND ${CMAKE_COMMAND} -E copy_if_different
            "${RAYLIB_PATH}/lib/raylib.dll"
            "$<TARGET_FILE_DIR:client>/raylib.dll"
        )
    endif()
endif()