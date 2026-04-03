package io.booking.booking_service.config;

import org.springframework.stereotype.Component;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

@Component
public class ReactiveTx {

    private final TransactionalOperator tx;
    private final ReactiveTransactionManager txManager;

    public ReactiveTx(TransactionalOperator tx, ReactiveTransactionManager txManager) {
        this.tx = tx;
        this.txManager = txManager;
    }

    /**
     * Default REQUIRED transaction
     */
    public <T> Mono<T> required(Supplier<Mono<T>> work) {
        return work.get().as(tx::transactional);
    }

    /**
     * Default REQUIRED transaction
     */
    public <T> Flux<T> requiredMany(Supplier<Flux<T>> work) {
        return work.get().as(tx::transactional);
    }

    /**
     * Customizable transaction definition (isolation/timeout/etc)
     */
    public <T> Mono<T> withDefinition(DefaultTransactionDefinition def, Supplier<Mono<T>> work) {
        TransactionalOperator custom = TransactionalOperator.create(txManager, def);
        return work.get().as(custom::transactional);
    }

    /**
     * Customizable transaction definition (isolation/timeout/etc)
     */
    public <T> Flux<T> withDefinitionMany(DefaultTransactionDefinition def, Supplier<Flux<T>> work) {
        TransactionalOperator custom = TransactionalOperator.create(txManager, def);
        return work.get().as(custom::transactional);
    }

    public static DefaultTransactionDefinition readCommitted() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        return def;
    }
}