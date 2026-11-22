package cl.cappucare.backend;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class MedicamentoControllerTest {
 @Test void listaMedicamentosConFormatoCompatibleConRetrofit(){
  JdbcTemplate db=mock(JdbcTemplate.class);
  when(db.queryForList("SELECT * FROM medicamentos")).thenReturn(List.of(Map.of("codigo","QR-01","nombre","Paracetamol","stock",5)));
  var respuesta=new MedicamentoController(db).listar();
  assertEquals(1,((List<?>)respuesta.get("medicamentos")).size());
  verify(db).queryForList("SELECT * FROM medicamentos");
 }
 @Test void codigoInexistenteDevuelve404(){
  JdbcTemplate db=mock(JdbcTemplate.class);
  when(db.queryForList("SELECT * FROM medicamentos WHERE codigo=?","X")).thenReturn(List.of());
  ApiError error=assertThrows(ApiError.class,()->new MedicamentoController(db).buscar("X"));
  assertEquals(404,error.status.value());
 }
}
