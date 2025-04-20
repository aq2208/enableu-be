package com.capstone.enableu.custom.handler;

import com.capstone.enableu.custom.exception.BadRequestException;
import com.capstone.enableu.custom.exception.NotFoundException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;


@Component
public class ExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {

        if (ex instanceof NotFoundException) {
            return GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.NOT_FOUND)
                    .message(ex.getMessage())
                    .build();
        } else if (ex instanceof BadRequestException) {
            return GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.BAD_REQUEST)
                    .message(ex.getMessage())
                    .build();
        } else if (ex instanceof RuntimeException) {
            return GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.INTERNAL_ERROR)
                    .message(ex.getMessage())
                    .build();
        } else {
            return null;
        }
    }
}
