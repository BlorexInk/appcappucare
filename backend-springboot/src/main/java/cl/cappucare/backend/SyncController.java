package cl.cappucare.backend;
import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.web.bind.annotation.*;import org.springframework.web.multipart.MultipartFile;import org.springframework.http.*;import java.util.*;import java.nio.file.*;import java.io.IOException;
@RestController @RequestMapping("/api") public class SyncController {
 private final JdbcTemplate db; public SyncController(JdbcTemplate db){this.db=db;}
 private List<Map<String,Object>> productos(){List<Map<String,Object>> rows=db.queryForList("SELECT * FROM productos");for(var row:rows){Object imgs=row.get("imagenes");row.put("imagenes",imgs==null||imgs.toString().isEmpty()?List.of():Arrays.asList(imgs.toString().split(";")));}return rows;}
 @GetMapping("/productos") public Map<String,Object> productosGet(){return Map.of("productos",productos());}


 @DeleteMapping("/productos/{codigo}")
 public ResponseEntity<Map<String, Object>> eliminarProducto(
         @PathVariable String codigo) {

  int eliminados = db.update(
          "DELETE FROM productos WHERE codigo = ?",
          codigo
  );

  if (eliminados == 0) {
   return ResponseEntity.status(HttpStatus.NOT_FOUND)
           .body(Map.of("mensaje", "Producto no encontrado"));
  }

  return ResponseEntity.ok(
          Map.of("mensaje", "Producto eliminado correctamente")
  );
 }

 @GetMapping("/usuarios") public Map<String,Object> usuarios(){return Map.of("usuarios",db.queryForList("SELECT * FROM usuarios"));}
 @GetMapping("/configuracion") public Map<String,Object> config(){return Map.of("configuracion",configMap());}
 private Map<String,Object> configMap(){Map<String,Object> result=new HashMap<>();db.queryForList("SELECT * FROM configuracion").forEach(r->result.put(r.get("clave").toString(),r.get("valor")));return result;}
 private void guardarConfig(Map<String,Object> m){for(String key:List.of("logoAppUri","gamaColor")){Object v=m.get(key);if(v!=null&&!v.toString().isEmpty())db.update("INSERT OR REPLACE INTO configuracion(clave,valor) VALUES(?,?)",key,v.toString());}}
 @PostMapping("/configuracion") public Map<String,Object> configPost(@RequestBody Map<String,Object> m){guardarConfig(m);return Map.of("mensaje","Configuración de marca guardada en el servidor");}
 @PostMapping("/sync") public Map<String,Object> sync(@RequestBody Map<String,Object> payload){
 for(var p:items(payload.get("productos"))){db.update("INSERT OR REPLACE INTO productos(codigo,nombre,descripcion,valor,cantidadDisponible,unidadMedida,nivelMinimo,estadoStock,imagenes) VALUES(?,?,?,?,?,?,?,?,?)",str(p,"codigo",""),str(p,"nombre",""),str(p,"descripcion",""),p.getOrDefault("valor",0),p.getOrDefault("cantidadDisponible",0),str(p,"unidadMedida","un"),p.getOrDefault("nivelMinimo",10),str(p,"estadoStock","DISPONIBLE"),imgs(p.get("imagenes")));}
 for(var u:items(payload.get("usuarios"))){db.update("INSERT OR REPLACE INTO usuarios(username,password,nombreCompleto,rol,categoriaCargo,email,telefono,direccion,ubicacionBodega,fotoPerfilUrl,fechaRegistro,horaInicioSesion) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",str(u,"username",""),str(u,"password","1234"),str(u,"nombreCompleto",str(u,"username","")),str(u,"rol","Vendedor"),str(u,"categoriaCargo","Ventas y Mostrador"),str(u,"email",""),str(u,"telefono",""),str(u,"direccion",""),str(u,"ubicacionBodega","Bodega Central XXXX"),str(u,"fotoPerfilUrl",""),str(u,"fechaRegistro",""),str(u,"horaInicioSesion","Sesión Inactiva"));}
 if(payload.get("configuracion") instanceof Map<?,?> cfg){Map<String,Object> c=new HashMap<>();cfg.forEach((k,v)->c.put(k.toString(),v));guardarConfig(c);}
 var p=productos();var u=db.queryForList("SELECT * FROM usuarios");Map<String,Object> result=new LinkedHashMap<>();result.put("mensaje","Sincronización exitosa de productos, usuarios y configuración");result.put("totalProductos",p.size());result.put("totalUsuarios",u.size());result.put("productos",p);result.put("usuarios",u);result.put("configuracion",configMap());return result;
 }
 private static String str(Map<String,Object> m,String k,String fallback){Object v=m.get(k);return v==null?fallback:v.toString();}
 private static String imgs(Object o){if(o instanceof List<?> l)return String.join(";",l.stream().map(Object::toString).toList());return o==null?"":o.toString();}
 private static List<Map<String,Object>> items(Object o){if(!(o instanceof List<?> l))return List.of();List<Map<String,Object>> out=new ArrayList<>();for(Object v:l){if(v instanceof Map<?,?> m){Map<String,Object> a=new HashMap<>();m.forEach((k,val)->a.put(k.toString(),val));out.add(a);}}return out;}
 @PostMapping("/upload") public Map<String,String> upload(@RequestParam("imagen") MultipartFile file) throws IOException {Path dir=Path.of("uploads");Files.createDirectories(dir);String original=Optional.ofNullable(file.getOriginalFilename()).orElse("image.jpg");String ext=original.contains(".")?original.substring(original.lastIndexOf('.')):".jpg";String name="img-"+UUID.randomUUID()+ext.replaceAll("[^.a-zA-Z0-9]","");Files.copy(file.getInputStream(),dir.resolve(name));return Map.of("url","http://10.0.2.2:3000/uploads/"+name,"filename",name);}
}
