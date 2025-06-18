package org.ntqqrev.saltify.message.outgoing

import org.ntqqrev.saltify.Entity

interface PrivateMessageBuilder :
    Entity,
    TextFeature,
    FaceFeature,
    ImageFeature,
    RecordFeature,
    VideoFeature,
    ReplyFeature,
    ForwardFeature

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