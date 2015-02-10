package com.cronutils.mapper;

import com.google.common.annotations.VisibleForTesting;

//as a convention we consider MONDAY index to compare weekday conventions and perform conversions

@VisibleForTesting
class WeekDay {
    private int monday;
    private boolean firstDayIsZero;

    public WeekDay(int monday, boolean firstDayIsZero){
        this.monday = monday;
        this.firstDayIsZero = firstDayIsZero;
    }

    public int getMonday() {
        return monday;
    }

    public int map(WeekDay weekDay, int day){
        int result = monday - weekDay.getMonday() + day;
        if(result==0){
            if(firstDayIsZero){
                result = 0;
            }else{
                result=7;
            }
        }
        return result;
    }

    //TODO map into tests :)
    //One based
    //my monday is je 1, theirs is 2,
    //ask for 5: 1-2+5=4
    //ask for 1: 1-2+1=0 -> vrni 7
    //ask for 7: 1-2+7=6

    //I am one based, they are zero based, monday is 1 for both
    //mine is 1, their monday is 1
    // ask for 5: 1-1+5=5 ok
    // ask for 0: 1-1+0=0 -> vrni 7
    // ask for 6: 1-1+6=6

    //zero based, monday is 1 for me, 0 for them
    //ask for 5: 1-0+5=6
    //ask for 0: 1-0+0=1
    //ask for 6: 1-0+6=7

    //I am zero based!, they are 1 based, starting sunday both
    //1 is monday for me, 2 for them
    //ask for 5: 1-2+5=4
    //ask for 1: 1-2+1=0 ok
    //ask for 7: 1-2+7=6 ok
}
