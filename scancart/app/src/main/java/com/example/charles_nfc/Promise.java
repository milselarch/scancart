package com.example.charles_nfc;

import java.util.ArrayList;

public class Promise<T, E> {
    protected final int FAILED = -1;
    protected final int RUNNING = 0;
    protected final int SUCCESS = 1;
    protected volatile int status;

    protected Throwable error;
    protected T result;

    protected ArrayList<Promise<T, E>> callbacks;
    protected Resolver<E> resolver;

    Promise() {
        this.status = RUNNING;
    }

    /*
    Promise(Resolver<E> resolver) {
        super();
        this.resolver = resolver;
    }
    */

    public void resolve(T result) {
        this.status = SUCCESS;
        this.result = result;

        for (Promise<T, E> callback: callbacks) {
            Resolver<E> resolver = callback.getResolver();
            callback.onReady(result, resolver);
        }
    }

    public void reject(Throwable error) {
        this.status = FAILED;
        this.error = error;

        for (Promise<T, E> callback: callbacks) {
            callback.onError(error);
        }
    }

    // override to use as callback / chain more promises
    public void onReady(T result, Resolver<E> resolver) {}

    public int getStatus() {
        return status;
    }

    public void onError(Throwable error) {}

    protected class Resolver<M> {
        private final Promise<T, M> promise;

        Resolver(Promise<T, M> promise) {
            this.promise = promise;
        }
        public void resolve(T value) {
            promise.resolve(value);
        }
        public void reject(Throwable error) {
            promise.reject(error);
        }
    }

    public Resolver<E> getResolver() {
        if (this.resolver == null) {
            this.resolver = new Resolver<>(this);
        }
        return this.resolver;
    }

    Promise<T, E> then(Promise<T, E> promise) {
        if (this.status == RUNNING) {
            this.callbacks.add(promise);
        } else if (this.status == FAILED) {
            promise.onError(this.error);
        } else if (this.status == SUCCESS) {
            Resolver<E> resolver = promise.getResolver();
            promise.onReady(this.result, resolver);
        }

        return promise;
    }
}

class PromiseAll<T, E> extends Promise<T, E> {
    ArrayList<T> results;
    ArrayList<Promise<T,E>> promises;
    protected ArrayList<Promise<ArrayList<T>, E>> callbacks;
    protected Resolver<E> resolver;
    protected int unresolved;

    PromiseAll(ArrayList<Promise<T,E>> promises) {
        super();
        this.promises = promises;
        this.unresolved = promises.size();

        for (Promise<T, E> promise: promises) {
            promise.then(new Promise<T, E>() {
                @Override
                public void onReady(T result, Resolver<E> resolver) {
                    unresolved -= 1;
                    results.add(result);
                    if (unresolved == 0) {
                        // resolve PromiseAll when
                        // all promises have been resolved
                        PromiseAll.this.resolve(results);
                    }
                }
                @Override
                public void onError(Throwable error) {
                    if (this.getStatus() == this.RUNNING) {
                        PromiseAll.this.status = FAILED;
                        PromiseAll.this.onError(error);
                    }
                }
            });
        }
    }

    public void resolve(ArrayList<T> result) {
        this.status = SUCCESS;

        for (Promise<ArrayList<T>, E> callback: callbacks) {
            callback.onReady(this.results, callback.getResolver());
        }
    }

    public void reject(Throwable error) {
        this.status = FAILED;
        this.error = error;

        for (Promise<ArrayList<T>, E> callback: callbacks) {
            callback.onError(this.error);
        }
    }
}