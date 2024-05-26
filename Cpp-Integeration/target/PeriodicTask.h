//
// Created by behnam on 2024-03-11.
//

#ifndef CODEGENERATION_PERIODICTASK_H
#define CODEGENERATION_PERIODICTASK_H


#include <atomic>
#include <functional>
#include <thread>
#include <iostream>

class PeriodicTask {
    double WRITE_OUTPUT_START_TIME = 0.9;
public:
    PeriodicTask(long long _period, long long _offset, int _cpuId, int _priority, int _policy)
            :_execute(false), period(_period), offset(_offset), cpuId(_cpuId), priority(_priority), policy(_policy)
    {}

    ~PeriodicTask() {
        if( _execute.load(std::memory_order_acquire) ) {
            stop();
        };
    }

    void stop()
    {
        _execute.store(false, std::memory_order_release);
        if( _thd.joinable() )
            _thd.join();
    }

    void start( timespec *startTime, long long startUpTime, std::function<bool(void)> rACFunc, std::function<void(void)> wFunc)
    {
        if( _execute.load(std::memory_order_acquire) ) {
            stop();
        };
        _execute.store(true, std::memory_order_release);
        _thd = std::thread([this, startTime, startUpTime, rACFunc, wFunc]() {
            if(this->cpuId != -1){
                configCpuAffinity(this->cpuId);
            }
            if(this->priority != -1){
                setThreadPriority(this->priority);
            }

            struct timespec periodTime;
            periodTime.tv_sec = this->period/1000000000;
            periodTime.tv_nsec = this->period%1000000000;

            struct timespec outputWriteTime;
            long long outputWriteRelativeTime = this->period* WRITE_OUTPUT_START_TIME;
            outputWriteTime.tv_sec = (outputWriteRelativeTime)/1000000000;
            outputWriteTime.tv_nsec = (outputWriteRelativeTime)%1000000000;

            struct timespec _startTime;
            _startTime.tv_nsec = startTime->tv_nsec;
            _startTime.tv_sec = startTime->tv_sec;

            struct timespec _offset;
            long long _startUpTime = startUpTime + this->offset;
            _offset.tv_sec = _startUpTime/1000000000;
            _offset.tv_nsec = _startUpTime%1000000000;

            timespec_add(&_startTime, &_offset);
//            sleep_until_next_activation(&_startTime);
//
            struct timespec currentActivation(_startTime);
//            // record time of first job
//            clock_gettime(CLOCK_MONOTONIC, &currentActivation);

            while (_execute.load(std::memory_order_acquire)) {
                sleep_until_next_activation(&_startTime);
                clock_gettime(CLOCK_MONOTONIC, &currentActivation);
                timestamp[ind++] = currentActivation.tv_sec*1000000000+currentActivation.tv_nsec;
                bool activated = rACFunc();
                if(wFunc != nullptr && activated ){
                    timespec writeActivation(_startTime);
                    timespec_add(&writeActivation, &outputWriteTime);
                    sleep_until_next_activation(&writeActivation);
                    wFunc();
                }
                timespec_add(&_startTime, &periodTime);
            }
        });
    }

    void sleep_until_next_activation(struct timespec *currentActivation) {
        int err;
        do {
            // perform an absolute sleep until tsk->current_activation
            err = clock_nanosleep(CLOCK_MONOTONIC, TIMER_ABSTIME, currentActivation, NULL);
            // if err is nonzero, we might have woken up too early
        } while (err != 0 && errno == EINTR);
    }

    void timespec_add(struct timespec *a, struct timespec *b) {
        a->tv_sec += b->tv_sec;
        a->tv_nsec += b->tv_nsec;
        if (a->tv_nsec >= 1000000000UL) {
            a->tv_sec++;
            a->tv_nsec %= 1000000000UL;
        }
    }

    bool is_running() const noexcept {
        return ( _execute.load(std::memory_order_acquire) &&
                 _thd.joinable() );
    }


    void configCpuAffinity(int cpuId){
        cpu_set_t cpuSet;
        CPU_ZERO(&cpuSet);
        CPU_SET(cpuId, &cpuSet);
        int rc = pthread_setaffinity_np(_thd.native_handle(),sizeof(cpu_set_t), &cpuSet);
        if (rc != 0) {
            std::cerr << "Error calling pthread_setaffinity_np: " << rc << "\n";
        }
    }

    void setThreadPriority(int priority) {
        // Set thread priority using platform-specific code (Linux example)
        pthread_t threadId = _thd.native_handle();

        struct sched_param params;
        params.sched_priority = priority;

        if (int val = pthread_setschedparam(threadId, policy, &params) != 0) {
            std::cerr << val << " Error setting thread priority." << std::endl;
            // Handle the error accordingly
        }
    }

    void printTimestamp(){
        std::cout<<"****************"<<std::endl;
        for (int i = 0 ; i < ind; i++) {
            std::cout << timestamp[i] << std::endl;
        }
    }

private:
    long long period;
    long long offset;
    int cpuId;
    int priority;
    int policy;
    std::atomic<bool> _execute;
    std::thread _thd;
    long long timestamp[100000];
    int ind = 0;
};


#endif //CODEGENERATION_PERIODICTASK_H
