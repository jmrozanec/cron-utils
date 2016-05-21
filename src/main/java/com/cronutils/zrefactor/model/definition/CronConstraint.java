package com.cronutils.zrefactor.model.definition;

import com.cronutils.zrefactor.model.Cron;

public abstract class CronConstraint {
    private String description;

    public CronConstraint(String description){
        this.description = description;
    }

    public abstract boolean validate(Cron cron);

    public String getDescription() {
        return description;
    }
}
