package com.heyan.eziogo.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Here be dragons Created by Ezio on 2018/1/30 下午6:10
 */
@Target(ElementType.FIELD)
public @interface HeyanBindView {
    int value() default -1;
}
