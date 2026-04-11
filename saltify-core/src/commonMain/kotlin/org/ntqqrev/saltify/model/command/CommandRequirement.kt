package org.ntqqrev.saltify.model.command

public fun interface CommandRequirement {
    public fun satisfies(): Boolean

    public operator fun plus(that: CommandRequirement): CommandRequirement = and(that)

    public infix fun and(that: CommandRequirement): CommandRequirement = CommandRequirement {
        this.satisfies() && that.satisfies()
    }

    public infix fun or(that: CommandRequirement): CommandRequirement = CommandRequirement {
        this.satisfies() || that.satisfies()
    }

    public operator fun not(): CommandRequirement = CommandRequirement {
        !this.satisfies()
    }
}
