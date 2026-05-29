package com.company.precost.calc;

/**
 * 계산 엔진 내부 예외.
 * module-calc 는 Spring/도메인에 의존하지 않으므로 자체 예외를 사용한다.
 * (cost-simulation 모듈에서 BusinessException 으로 변환하여 처리)
 */
public class CalcException extends RuntimeException {

    public CalcException(String message) {
        super(message);
    }
}
