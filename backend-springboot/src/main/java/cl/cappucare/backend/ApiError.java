package cl.cappucare.backend;
import org.springframework.http.*;import org.springframework.web.bind.annotation.*;import java.util.Map;
class ApiError extends RuntimeException {final HttpStatus status;ApiError(HttpStatus status,String msg){super(msg);this.status=status;}}
@RestControllerAdvice class ApiErrors {@ExceptionHandler(ApiError.class) ResponseEntity<Map<String,String>> error(ApiError e){return ResponseEntity.status(e.status).body(Map.of("mensaje",e.getMessage()));}}
