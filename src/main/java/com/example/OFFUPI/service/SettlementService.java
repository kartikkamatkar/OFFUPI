package com.example.OFFUPI.service;

import com.example.OFFUPI.entity.Account;
import com.example.OFFUPI.entity.PaymentInstruction;
import com.example.OFFUPI.entity.Transaction;
import com.example.OFFUPI.repository.AccountRepository;
import com.example.OFFUPI.repository.TransactionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class SettlementService {

    private static final Logger log =
            LoggerFactory.getLogger(SettlementService.class);

    @Autowired
    private AccountRepository accounts;

    @Autowired
    private TransactionRepository transactions;

    @Autowired
    private MetricsService metricsService;

    @Transactional
    public Transaction settle(
            PaymentInstruction instruction,
            String packetHash,
            String bridgeNodeId,
            int hopCount
    ) {

        Account sender = accounts.findById(
                instruction.getSenderVpa()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Unknown sender VPA: "
                                + instruction.getSenderVpa()
                )
        );

        Account receiver = accounts.findById(
                instruction.getReceiverVpa()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Unknown receiver VPA: "
                                + instruction.getReceiverVpa()
                )
        );

        BigDecimal amount =
                instruction.getAmount();

        if (amount.signum() <= 0) {

            metricsService.incrementFailure();

            throw new IllegalArgumentException(
                    "Amount must be positive"
            );
        }

        if (sender.getBalance().compareTo(amount) < 0) {

            log.warn(
                    "Insufficient balance: {} has ₹{}, tried to send ₹{}",
                    sender.getVpa(),
                    sender.getBalance(),
                    amount
            );

            metricsService.incrementFailure();

            return recordRejected(
                    instruction,
                    packetHash,
                    bridgeNodeId,
                    hopCount
            );
        }

        sender.setBalance(
                sender.getBalance().subtract(amount)
        );

        receiver.setBalance(
                receiver.getBalance().add(amount)
        );

        accounts.save(sender);

        accounts.save(receiver);

        Transaction tx = new Transaction();

        tx.setPacketHash(packetHash);

        tx.setSenderVpa(
                instruction.getSenderVpa()
        );

        tx.setReceiverVpa(
                instruction.getReceiverVpa()
        );

        tx.setAmount(amount);

        tx.setSignedAt(
                Instant.ofEpochMilli(
                        instruction.getSignedAt()
                )
        );

        tx.setSettledAt(
                Instant.now()
        );

        tx.setBridgeNodeId(
                bridgeNodeId
        );

        tx.setHopCount(
                hopCount
        );

        tx.setStatus(
                Transaction.Status.SETTLED
        );

        transactions.save(tx);

        metricsService.incrementSuccess();

        log.info(
                "SETTLED ₹{} from {} to {}",
                amount,
                sender.getVpa(),
                receiver.getVpa()
        );

        return tx;
    }

    private Transaction recordRejected(
            PaymentInstruction instruction,
            String packetHash,
            String bridgeNodeId,
            int hopCount
    ) {

        Transaction tx = new Transaction();

        tx.setPacketHash(packetHash);

        tx.setSenderVpa(
                instruction.getSenderVpa()
        );

        tx.setReceiverVpa(
                instruction.getReceiverVpa()
        );

        tx.setAmount(
                instruction.getAmount()
        );

        tx.setSignedAt(
                Instant.ofEpochMilli(
                        instruction.getSignedAt()
                )
        );

        tx.setSettledAt(
                Instant.now()
        );

        tx.setBridgeNodeId(
                bridgeNodeId
        );

        tx.setHopCount(
                hopCount
        );

        tx.setStatus(
                Transaction.Status.REJECTED
        );

        return transactions.save(tx);
    }
}