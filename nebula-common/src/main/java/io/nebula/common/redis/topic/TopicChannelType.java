package io.nebula.common.redis.topic;

public enum TopicChannelType {
    DEFAULT("default", 5050L),
    MARQUEE("MARQUEE", 5051L),
    PIPEI("PIPEI", 5052L),
    ;

    private final String channel;
    private final long queueId;

    TopicChannelType(String channel, long queueId) {
        this.channel = channel;
        this.queueId = queueId;
    }

    public String getChannel() { return channel; }
    public long getQueueId() { return queueId; }
}
