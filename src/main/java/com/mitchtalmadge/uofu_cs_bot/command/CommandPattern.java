package com.mitchtalmadge.uofu_cs_bot.command;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface CommandPattern {

  /** @return The command pattern to match. */
  String[] value();

  /** @return True if this command must match exactly (No extra arguments). */
  boolean strict() default false;
}
