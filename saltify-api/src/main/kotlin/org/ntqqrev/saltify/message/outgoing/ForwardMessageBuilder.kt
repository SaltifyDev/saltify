package org.ntqqrev.saltify.message.outgoing

import org.ntqqrev.saltify.Entity

interface ForwardMessageBuilder :
    Entity,
    TextFeature,
    FaceFeature,
    ImageFeature,
    VideoFeature,
    ReplyFeature,
    ForwardFeature