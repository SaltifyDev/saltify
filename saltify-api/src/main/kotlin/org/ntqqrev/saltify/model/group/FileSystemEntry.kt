package org.ntqqrev.saltify.model.group

import org.ntqqrev.saltify.Entity
import org.ntqqrev.saltify.model.Group

interface FileSystemEntry : Entity {
    /**
     * The group the file system entry belongs to.
     */
    val group: Group
}