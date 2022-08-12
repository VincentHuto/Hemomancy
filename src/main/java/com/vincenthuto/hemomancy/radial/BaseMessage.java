package com.vincenthuto.hemomancy.radial;

public  abstract class BaseMessage {
    protected boolean messageIsValid = false;

    public final boolean isMessageValid() {
        return this.messageIsValid;
    }
}