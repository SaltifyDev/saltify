package org.ntqqrev.saltify.message.outgoing

import org.ntqqrev.saltify.Entity

interface GroupMessageBuilder :
    Entity,
    TextFeature,
    MentionFeature,
    FaceFeature,
    ImageFeature,
    RecordFeature,
    VideoFeature,
    ReplyFeature,
    ForwardFeature