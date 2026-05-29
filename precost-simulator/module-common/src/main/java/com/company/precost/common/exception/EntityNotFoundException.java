package com.company.precost.common.exception;

/**
 * 엔티티 조회 실패 예외.
 */
public class EntityNotFoundException extends BusinessException {

    public EntityNotFoundException(String message) {
        super(ErrorCode.ENTITY_NOT_FOUND, message);
    }

    public EntityNotFoundException(String entityName, Object id) {
        super(ErrorCode.ENTITY_NOT_FOUND,
                String.format("%s(id=%s)를 찾을 수 없습니다.", entityName, id));
    }
}
