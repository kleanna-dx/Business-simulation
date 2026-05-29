package com.company.precost.common.result;

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 성공/실패를 명시적으로 표현하는 Result 패턴 (선택적 사용).
 * 예외를 던지기 애매한 경우(검증 다건 등)에 사용한다.
 *
 * @param <T> 성공 값 타입
 * @param <E> 실패 값 타입
 */
public abstract class Result<T, E> {

    public abstract boolean isSuccess();

    public boolean isFailure() {
        return !isSuccess();
    }

    public abstract T getValue();

    public abstract E getError();

    public static <T, E> Result<T, E> success(T value) {
        return new Success<>(value);
    }

    public static <T, E> Result<T, E> failure(E error) {
        return new Failure<>(error);
    }

    public <U> Result<U, E> map(Function<? super T, ? extends U> mapper) {
        if (isSuccess()) {
            return success(mapper.apply(getValue()));
        }
        return failure(getError());
    }

    public T orElseGet(Supplier<? extends T> supplier) {
        return isSuccess() ? getValue() : supplier.get();
    }

    // --- 내부 구현 ---
    private static final class Success<T, E> extends Result<T, E> {
        private final T value;

        private Success(T value) {
            this.value = value;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public E getError() {
            throw new NoSuchElementException("Success 에는 error 가 없습니다.");
        }
    }

    private static final class Failure<T, E> extends Result<T, E> {
        private final E error;

        private Failure(E error) {
            this.error = error;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public T getValue() {
            throw new NoSuchElementException("Failure 에는 value 가 없습니다.");
        }

        @Override
        public E getError() {
            return error;
        }
    }
}
