package io.github.idoomful.assassinsduels.data.SQL;

public interface Callback<T> {
    void done(T result);
}
