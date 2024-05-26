cmake_minimum_required(VERSION 3.22)

project(mimosCodeGen)

set(CMAKE_CXX_STANDARD 17)

add_executable(mimosCodeGen main.cpp)

TARGET_LINK_LIBRARIES( mimosCodeGen pthread)
