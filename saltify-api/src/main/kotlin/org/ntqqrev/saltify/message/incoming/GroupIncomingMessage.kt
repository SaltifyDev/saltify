package org.ntqqrev.saltify.message.incoming

import org.ntqqrev.saltify.model.Group
import org.ntqqrev.saltify.model.GroupMember

interface GroupIncomingMessage : IncomingMessage {
    /**
     * The group where the message was sent.
     */
    val group: Group

    /**
     * The member who sent the message.
     */
    val sender: GroupMember
}