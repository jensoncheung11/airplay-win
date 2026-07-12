package com.github.serezhka.airplay.server.internal.handler.audio;

import com.github.serezhka.airplay.lib.AirPlay;
import com.github.serezhka.airplay.server.AirPlayConsumer;
import com.github.serezhka.airplay.server.internal.packet.AudioPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
public class AudioHandler extends ChannelInboundHandlerAdapter {

    private final AirPlay airPlay;
    private final AirPlayConsumer dataConsumer;

    private final AudioPacket[] buffer = new AudioPacket[512];

    private int prevSeqNum;
    private int packetsInBuffer;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        AudioPacket packet = (AudioPacket) msg;

        int curSeqNum = packet.getSequenceNumber();
        if (curSeqNum <= prevSeqNum) {
            return;
        }

        buffer[curSeqNum % buffer.length] = packet;
        packetsInBuffer++;

        while (drainNextAvailablePacket()) {
        }
    }

    private boolean drainNextAvailablePacket() throws Exception {
        int nextSeqNum = prevSeqNum == 0 ? firstAvailableSequenceNumber() : prevSeqNum + 1;
        if (nextSeqNum < 0) {
            return false;
        }

        AudioPacket nextPacket = buffer[nextSeqNum % buffer.length];
        if (nextPacket != null && nextPacket.isAvailable()) {
            consume(nextSeqNum, nextPacket);
            return true;
        }

        if (packetsInBuffer > 1) {
            int resumedSeqNum = firstAvailableSequenceNumberAfter(nextSeqNum);
            if (resumedSeqNum > 0) {
                log.debug("Skipping missing audio packet(s) from {} to {}", nextSeqNum, resumedSeqNum - 1);
                prevSeqNum = resumedSeqNum - 1;
                return true;
            }
        }

        return false;
    }

    private int firstAvailableSequenceNumber() {
        for (AudioPacket packet : buffer) {
            if (packet != null && packet.isAvailable()) {
                return packet.getSequenceNumber();
            }
        }
        return -1;
    }

    private int firstAvailableSequenceNumberAfter(int sequenceNumber) {
        int candidate = Integer.MAX_VALUE;
        for (AudioPacket packet : buffer) {
            if (packet != null && packet.isAvailable() && packet.getSequenceNumber() > sequenceNumber) {
                candidate = Math.min(candidate, packet.getSequenceNumber());
            }
        }
        return candidate == Integer.MAX_VALUE ? -1 : candidate;
    }

    private void consume(int sequenceNumber, AudioPacket audioPacket) throws Exception {
        airPlay.decryptAudio(audioPacket.getEncodedAudio(), audioPacket.getEncodedAudioSize());
        dataConsumer.onAudio(Arrays.copyOfRange(audioPacket.getEncodedAudio(), 0, audioPacket.getEncodedAudioSize()));
        audioPacket.available(false);
        prevSeqNum = sequenceNumber;
        packetsInBuffer--;
    }
}
