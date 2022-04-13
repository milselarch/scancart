package com.example.charles_nfc;

import android.util.Pair;

import java.util.ArrayList;

abstract class Resolvable<T, E> {
    protected final int FAILED = -1;
    protected final int RUNNING = 0;
    protected final int SUCCESS = 1;
    protected volatile int status;

    protected Resolver<E> resolver;
    protected Throwable error;

    Resolvable() {
        this.status = RUNNING;
    }

    public int getStatus() {
        return status;
    }

    protected class Resolver<M extends E> {
        private final Resolvable<T, M> promise;

        Resolver(Resolvable<T, M> promise) {
            this.promise = promise;
        }

        public void resolve(M value) {
            promise.resolve(value);
        }

        public void reject(Throwable error) {
            promise.reject(error);
        }
    }

    public Resolver<E> getResolver() {
        if (this.resolver == null) {
            this.resolver = new Resolver<E>(this);
        }
        return this.resolver;
    }

    abstract boolean resolve(E value);
    abstract boolean reject(Throwable error);
    abstract void onReady(T result, Resolver<E> resolver);
}

public abstract class Promise<T, E, B> extends Resolvable<T, E> {
    protected E result;
    final protected ArrayList<Promise<E, B, ?>> followUps = new ArrayList<>();

    Promise() {
        this(false);
    }

    Promise(boolean start) {
        if (start) {
            this.status = SUCCESS;
        }
    }

    abstract public void onReady(T result, Resolver<E> resolver);
    abstract public void onError(Throwable error);

    @Override
    public boolean resolve(E result) {
        return this.resolvePromise(result);
    }

    public boolean resolvePromise(E result) {
        // promise cannot be resolved more than once
        if (this.status != RUNNING) {
            return false;
        }

        this.status = SUCCESS;
        this.result = result;

        for (Promise<E, B, ?> callback: followUps) {
            Promise<E, B, ?>.Resolver<B> resolver = callback.getResolver();
            callback.onReady(result, resolver);
        }

        return true;
    }

    public boolean reject(Throwable error) {
        // promise cannot be rejected more than once
        if (this.status != RUNNING) {
            return false;
        }

        this.status = FAILED;
        this.error = error;

        for (Promise<E, B, ?> callback: followUps) {
            callback.onError(error);
            callback.reject(error);
        }

        return true;
    }

    public Promise<E, B, ?> then(Promise<E, B, ?> promise) {
        if (this.status == RUNNING) {
            this.followUps.add(promise);
        } else if (this.status == FAILED) {
            promise.onError(this.error);
        } else if (this.status == SUCCESS) {
            Resolvable<E, B>.Resolver<B> resolver = promise.getResolver();
            promise.onReady(this.result, resolver);
        }

        return promise;
    }

    public Promise<ArrayList<E>, ArrayList<Pair<E,B>>, ?> awaitAll(
        ArrayList<Promise<E, B, E>> promises
    ) {
        /*
        E is datatype of values of this Promise
        Pair<E,B> is datatype of ArrayList that aggregator Promise resolves with
        */
        final int numPromises = promises.size();

        return new Promise<
            ArrayList<E>, ArrayList<Pair<E, B>>, Object
        >() {
            @Override
            public void onReady(
                ArrayList<E> inputs, Resolver<ArrayList<Pair<E, B>>> completer
            ) {
                ArrayList<Pair<E, B>> results = new ArrayList<>();

                for (int k=0; k<inputs.size(); k++) {
                    Promise<E, B, E> promise = promises.get(k);
                    E input = inputs.get(k);

                    promise.then(new Promise<B, E, Object>() {
                        @Override
                        public void onReady(B result, Resolver<E> resolver) {
                            results.add(new Pair<E, B>(input, result));
                            if (results.size() == numPromises) {
                                completer.resolve(results);
                            }
                        }

                        @Override
                        public void onError(Throwable error) {
                            completer.reject(error);
                        }
                    });
                }
            }

            @Override
            public void onError(Throwable error) {

            }
        };
    }
}
