
#pragma once
#include <atomic>
#define CNT 1
template<class T>
class SPSCRegister
{
public:
    SPSCRegister(T _data):data(_data){}

    T* pop() {
        data_cache = ((std::atomic<T>*)&data)->load(std::memory_order_consume);
        return &data_cache;
    }

    void push(T newData) {
      ((std::atomic<T>*)&data)->store(newData, std::memory_order_release);
    }

  private:
    alignas(128) T data;
    alignas(128) T data_cache;
};

