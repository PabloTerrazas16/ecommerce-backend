# E-Commerce Backend - Spring Boot API

Frontend: https://github.com/UniDasp/react-ecommerce

## 📋 Descripción

Backend completo de e-commerce desarrollado con **Spring Boot 3.2.0** y **Java 17**. Implementa autenticación JWT, gestión de productos, categorías, usuarios, roles, pagos y auditoría completa con arquitectura de capas (Controller - Service - Repository).

---

## 🛠️ Tecnologías Utilizadas

- **Java**: 17 (Eclipse Adoptium)
- **Spring Boot**: 3.2.0
- **Spring Security**: Autenticación y autorización con JWT
- **Spring Data JPA**: Persistencia de datos
- **Spring AOP**: Auditoría con Aspect Oriented Programming
- **Springdoc OpenAPI**: Documentación Swagger UI automática 🆕
- **MySQL**: Base de datos relacional (vía Laragon)
- **Maven**: Gestión de dependencias
- **Lombok**: Reducción de código boilerplate
- **JJWT**: Generación y validación de tokens JWT (v0.12.3)
- **Jackson**: Serialización/Deserialización JSON
- **Hibernate**: ORM para mapeo objeto-relacional
- **Jakarta Validation**: Validaciones con anotaciones

---

## Estructura del Proyecto

```
ecommerce-backend/
├── src/main/java/com/ecommerce/levelup/
│   ├── EcommerceBackendApplication.java    # Clase principal
│   │
│   ├── auth/                                # Módulo de Autenticación
│   │   ├── controller/
│   │   │   ├── AuthController.java          # Login, Register, Refresh Token
│   │   │   └── DebugController.java         # Endpoints de debug (temporal)
│   │   ├── dto/
│   │   │   ├── LoginRequest.java            # DTO para login
│   │   │   ├── LoginResponse.java           # DTO respuesta con JWT y roles
│   │   │   ├── RegisterRequest.java         # DTO para registro
│   │   │   └── ChangePasswordRequest.java   # DTO para cambio de contraseña
│   │   └── service/
│   │       ├── AuthService.java             # Lógica de autenticación
│   │       └── CustomUserDetailsService.java # Carga de usuarios para Spring Security
│   │
│   ├── config/                              # Configuraciones Globales
│   │   ├── CorsConfig.java                  # Configuración CORS para React
│   │   ├── DataInitializer.java             # Datos iniciales (roles, admin user)
│   │   └── GlobalExceptionHandler.java      # Manejo centralizado de excepciones
│   │
│   ├── security/                            # Módulo de Seguridad
│   │   ├── JwtUtil.java                     # Utilidad para JWT (generar, validar)
│   │   ├── JwtFilter.java                   # Filtro para interceptar y validar JWT
│   │   └── SecurityConfig.java              # Configuración de Spring Security
│   │
│   ├── user/                                # Módulo de Usuarios y Roles
│   │   ├── controller/
│   │   │   ├── UserController.java          # CRUD de usuarios
│   │   │   └── RoleController.java          # CRUD de roles
│   │   ├── dto/
│   │   │   ├── UserDTO.java                 # DTO de usuario
│   │   │   ├── RoleDTO.java                 # DTO de rol
│   │   │   └── CreateRoleRequest.java       # DTO para crear rol
│   │   ├── model/
│   │   │   ├── User.java                    # Entidad Usuario
│   │   │   └── Role.java                    # Entidad Rol
│   │   ├── repository/
│   │   │   ├── UserRepository.java          # Acceso a datos de usuarios
│   │   │   └── RoleRepository.java          # Acceso a datos de roles
│   │   └── service/
│   │       ├── UserService.java             # Lógica de negocio de usuarios
│   │       └── RoleService.java             # Lógica de negocio de roles
│   │
│   ├── product/                             # Módulo de Productos
│   │   ├── controller/
│   │   │   ├── ProductController.java       # CRUD de productos
│   │   │   └── CategoryController.java      # CRUD de categorías
│   │   ├── dto/
│   │   │   ├── ProductDTO.java              # DTO de producto
│   │   │   └── CategoryDTO.java             # DTO de categoría
│   │   ├── model/
│   │   │   ├── Product.java                 # Entidad Producto
│   │   │   └── Category.java                # Entidad Categoría
│   │   ├── repository/
│   │   │   ├── ProductRepository.java       # Acceso a datos de productos
│   │   │   └── CategoryRepository.java      # Acceso a datos de categorías
│   │   └── service/
│   │       ├── ProductService.java          # Lógica de productos + SKU auto
│   │       └── CategoryService.java         # Lógica de categorías + código auto
│   │
│   ├── payment/                             # Módulo de Pagos
│   │   ├── controller/
│   │   │   └── PaymentController.java       # Procesar pagos, reembolsos
│   │   ├── dto/
│   │   │   ├── PaymentDTO.java              # DTO de pago
│   │   │   ├── PaymentTokenRequest.java     # DTO para generar token de pago
│   │   │   ├── PaymentTokenResponse.java    # DTO respuesta token
│   │   │   └── ProcessPaymentRequest.java   # DTO solicitud de pago
│   │   ├── model/
│   │   │   └── Payment.java                 # Entidad Pago
│   │   ├── repository/
│   │   │   └── PaymentRepository.java       # Acceso a datos de pagos
│   │   └── service/
│   │       └── PaymentService.java          # Lógica de pagos
│   │
│   └── audit/                               # Módulo de Auditoría
│       ├── AuditLog.java                    # Entidad de registro de auditoría
│       ├── AuditLogRepository.java          # Acceso a datos de auditoría
│       ├── AuditService.java                # Servicio de auditoría
│       ├── AuditAspect.java                 # AOP para interceptar operaciones
│       └── AuditController.java             # Consultar logs de auditoría
│
└── src/main/resources/
    └── application.properties               # Configuración de la aplicación
```

---

## 🔐 Módulo de Autenticación (auth)

### **JWT (JSON Web Token)**

El sistema utiliza tokens JWT para autenticar usuarios sin mantener sesiones en el servidor.

#### **Tipos de Tokens**

1. **TOKEN USER**: Para autenticación de usuarios (login, acceso a recursos)
2. **TOKEN PAYMENT**: Para autorizar transacciones de pago

#### **Flujo de Autenticación**

```
1. Usuario → POST /autenticacion/login (username, password)
2. AuthService valida credenciales
3. JwtUtil genera token JWT con username y roles
4. Cliente recibe: { token, type: "Bearer", username, email, roles: [...] }
5. Cliente incluye token en headers: Authorization: Bearer <token>
6. JwtFilter intercepta requests y valida token
7. Si válido, extrae usuario y roles → permite acceso
```

#### **Endpoints de Autenticación**

| Método | Endpoint                            | Descripción                   | Auth |
| ------ | ----------------------------------- | ----------------------------- | ---- |
| POST   | `/autenticacion/registrar`          | Registrar nuevo usuario       | ❌   |
| POST   | `/autenticacion/login`              | Iniciar sesión (devuelve JWT) | ❌   |
| POST   | `/autenticacion/refrescar`          | Refrescar token expirado      | ✅   |
| GET    | `/autenticacion/validar`            | Validar token                 | ✅   |
| GET    | `/autenticacion/yo`                 | Obtener usuario actual        | ✅   |
| POST   | `/autenticacion/cambiar-contrasena` | Cambiar contraseña            | ✅   |

#### **Ejemplo de Login**

**Request:**

```json
POST /autenticacion/login
{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**

```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "type": "Bearer",
  "username": "admin",
  "email": "admin@ecommerce.com",
  "roles": ["ROLE_ADMIN", "ROLE_USER"]
}
```

#### **Ejemplo de Cambio de Contraseña** 🆕

**Request:**

```json
POST /autenticacion/cambiar-contrasena
Authorization: Bearer <token>
{
  "currentPassword": "admin123",
  "newPassword": "nuevaSegura123",
  "confirmPassword": "nuevaSegura123"
}
```

**Response:**

```json
{
  "mensaje": "Contraseña cambiada exitosamente"
}
```

**Validaciones:**

- ✅ Contraseña actual correcta
- ✅ Nueva contraseña diferente a la actual
- ✅ Mínimo 6 caracteres
- ✅ Confirmación coincide con nueva contraseña

#### **Componentes Clave**

- **JwtUtil.java**:

  - `generateToken(username)`: Crea JWT con expiración de 24h
  - `validateToken(token)`: Verifica firma y expiración
  - `getUsernameFromToken(token)`: Extrae usuario del payload
  - `getTokenType(token)`: Identifica tipo de token (USER/PAYMENT)

- **JwtFilter.java**:

  - Intercepta TODOS los requests
  - Extrae token del header `Authorization`
  - Valida token y carga usuario en `SecurityContext`

- **AuthService.java**:
  - `register()`: Crea usuario nuevo con rol `ROLE_USER`
  - `login()`: Autentica y devuelve JWT + roles
  - Usa `@Transactional` para manejar lazy loading de roles

---

## 🔒 Módulo de Seguridad (security)

### **Spring Security + JWT**

Configuración que combina Spring Security con autenticación JWT stateless.

#### **SecurityConfig.java**

Define qué rutas son públicas y cuáles requieren autenticación/roles:

```java
// Rutas públicas (sin token)
.requestMatchers("/autenticacion/**").permitAll()
.requestMatchers("/debug/**").permitAll()
.requestMatchers("/productos").permitAll()  // GET público
.requestMatchers("/categorias").permitAll() // GET público

// Rutas autenticadas (requieren token válido)
.requestMatchers("/api/users/**").authenticated()
.requestMatchers("/api/payments/**").authenticated()

// Rutas de admin (requieren ROLE_ADMIN)
.requestMatchers("/api/admin/**").hasRole("ADMIN")

// Todo lo demás requiere autenticación
.anyRequest().authenticated()
```

#### **Roles del Sistema**

| Rol              | Descripción      | Permisos                                 |
| ---------------- | ---------------- | ---------------------------------------- |
| `ROLE_USER`      | Usuario estándar | Ver productos, comprar, gestionar perfil |
| `ROLE_ADMIN`     | Administrador    | CRUD productos/categorías, ver pagos     |
| `ROLE_MODERATOR` | Moderador        | Revisar contenido, gestionar usuarios    |

#### **Anotaciones de Seguridad**

```java
@PreAuthorize("hasRole('ADMIN')")  // Solo admin
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")  // Admin o Moderator
```

#### **Filtro de Autenticación**

1. **JwtFilter** se ejecuta ANTES de cualquier controller
2. Extrae token: `Authorization: Bearer <token>`
3. Valida token con `JwtUtil`
4. Si válido, carga `UserDetails` y establece `Authentication`
5. Spring Security permite/deniega acceso según roles

---

## Módulo de Productos (product)

### **Entidades**

#### **Product.java**

```json
{
  "id": 1,
  "code": "JM001",
  "name": "Catan",
  "price": 29990,
  "categoryId": 1,
  "category": "Juegos de Mesa",
  "image": "https://...",
  "description": "...",
  "stock": 10,
  "featured": false,
  "active": true
}
```

#### **Category.java**

```json
{
  "id": 1,
  "name": "Juegos de Mesa",
  "code": "JM",
  "description": "...",
  "active": true
}
```

### **Generación Automática de SKU**

El sistema genera códigos únicos automáticamente:

1. **Categoría se crea con código**: `"code": "JM"` (Juegos de Mesa)
2. **Producto se crea sin code**: Se genera automáticamente
3. **Algoritmo**: Busca último número en esa categoría + 1
4. **Formato**: `CODIGO_CATEGORIA + 3 dígitos` → `JM001`, `JM002`, etc.

**Ejemplo:**

```
Categoría: Juegos de Mesa (code: "JM")
Productos: JM001, JM002, JM003...

Categoría: Accesorios (code: "AC")
Productos: AC001, AC002, AC003...
```

#### **ProductService.java - Lógica Principal**

- `createProduct()`: Crea producto y genera SKU automáticamente
- `generateSku(category)`: Busca último SKU de la categoría y genera siguiente
- `updateProduct()`: Actualiza producto (no regenera SKU)
- `searchProducts()`: Búsqueda por nombre, descripción o código
- `getActiveProducts()`: Solo productos activos

### **Endpoints de Productos**

| Método | Endpoint                           | Descripción                 | Auth     |
| ------ | ---------------------------------- | --------------------------- | -------- |
| GET    | `/productos`                       | Listar todos los productos  | ❌       |
| GET    | `/productos/{id}`                  | Obtener producto por ID     | ❌       |
| GET    | `/productos/categoria/{id}`        | Productos de una categoría  | ❌       |
| GET    | `/productos/buscar?consulta=texto` | Buscar productos            | ❌       |
| GET    | `/productos/activos`               | Solo productos activos      | ❌       |
| POST   | `/productos`                       | Crear producto (genera SKU) | ✅ ADMIN |
| PUT    | `/productos/{id}`                  | Actualizar producto         | ✅ ADMIN |
| PATCH  | `/productos/{id}/stock`            | Actualizar stock solamente  | ✅ ADMIN |
| DELETE | `/productos/{id}`                  | Eliminar producto           | ✅ ADMIN |

### **Endpoints de Categorías**

| Método | Endpoint              | Descripción                   | Auth     |
| ------ | --------------------- | ----------------------------- | -------- |
| GET    | `/categorias`         | Listar todas las categorías   | ❌       |
| GET    | `/categorias/{id}`    | Obtener categoría por ID      | ❌       |
| GET    | `/categorias/activas` | Solo categorías activas       | ❌       |
| POST   | `/categorias`         | Crear categoría (genera code) | ✅ ADMIN |
| PUT    | `/categorias/{id}`    | Actualizar categoría          | ✅ ADMIN |
| DELETE | `/categorias/{id}`    | Eliminar categoría            | ✅ ADMIN |

---

## Módulo de Usuarios (user)

### **Entidades**

#### **User.java**

```json
{
  "id": 1,
  "username": "admin",
  "email": "admin@ecommerce.com",
  "firstName": "Admin",
  "lastName": "Sistema",
  "fullName": "Admin Sistema",
  "phone": "123456789",
  "address": "Calle Principal 123",
  "region": "Metropolitana",
  "city": "Santiago",
  "enabled": true,
  "roles": ["ROLE_ADMIN"],
  "createdAt": "2025-11-17T10:00:00",
  "updatedAt": "2025-11-17T10:00:00"
}
```

- Relación `@ManyToMany` con `Role`
- Tabla intermedia: `user_roles`
- Campos: `username`, `email`, `password`, `firstName`, `lastName`, `phone`, `address`, `region`, `city`, `enabled`
- Campo calculado: `fullName` (firstName + lastName)
- **Problema resuelto**: Lazy loading de roles con `@Transactional` y `JOIN FETCH`

#### **Role.java**

- `@JsonIgnore` en relación inversa para evitar ciclos JSON
- `@EqualsAndHashCode(exclude = "users")` para evitar StackOverflow
- Campos: `name`, `description`

### **Relación User-Role**

```sql
users (id, username, email, password, ...)
roles (id, name, description)
user_roles (user_id, role_id)  -- Tabla intermedia
```

**Fix aplicado para roles vacíos:**

1. `UserRepository.findByUsernameWithRoles()` con `@Query` + `JOIN FETCH`
2. `@Transactional(readOnly = true)` en `AuthService.login()`
3. `user.getRoles().size()` para forzar carga dentro de transacción

### **Endpoints de Usuarios**

| Método | Endpoint                | Descripción                | Auth           |
| ------ | ----------------------- | -------------------------- | -------------- |
| GET    | `/usuarios`             | Listar todos los usuarios  | ✅ ADMIN       |
| GET    | `/usuarios/{id}`        | Obtener usuario por ID     | ✅ ADMIN       |
| PUT    | `/usuarios/{id}`        | Actualizar usuario         | ✅ ADMIN/Owner |
| PATCH  | `/usuarios/{id}/estado` | Activar/Desactivar usuario | ✅ ADMIN       |
| DELETE | `/usuarios/{id}`        | Eliminar usuario           | ✅ ADMIN       |

### **Campos de Usuario**

- `username` _(único, 3-50 caracteres)_
- `email` _(único, formato válido)_
- `password` _(mínimo 6 caracteres, encriptado)_
- `firstName` _(obligatorio)_
- `lastName` _(obligatorio)_
- `fullName` _(calculado automáticamente)_
- `phone` _(opcional, máx. 20 caracteres)_
- `address` _(opcional, máx. 200 caracteres)_
- `region` _(opcional, máx. 100 caracteres)_ 🆕
- `city` _(opcional, máx. 100 caracteres)_ 🆕
- `enabled` _(boolean, default: true)_

---

## 🎭 Módulo de Roles (user/roles)

### **Gestión Completa de Roles** 🆕

El sistema permite crear, editar y eliminar roles personalizados, además de los roles del sistema.

### **Entidades**

#### **RoleDTO.java**

```json
{
  "id": 1,
  "name": "ROLE_ADMIN",
  "description": "Administrador del sistema",
  "userCount": 5
}
```

### **Endpoints de Roles**

| Método | Endpoint      | Descripción            | Auth     |
| ------ | ------------- | ---------------------- | -------- |
| GET    | `/roles`      | Listar todos los roles | ✅ ADMIN |
| GET    | `/roles/{id}` | Obtener rol por ID     | ✅ ADMIN |
| POST   | `/roles`      | Crear nuevo rol        | ✅ ADMIN |
| PUT    | `/roles/{id}` | Actualizar rol         | ✅ ADMIN |
| DELETE | `/roles/{id}` | Eliminar rol           | ✅ ADMIN |

### **Reglas de Negocio**

✅ **Crear Rol:**

- Nombre debe empezar con `ROLE_`
- Solo mayúsculas y guiones bajos
- Nombre único en el sistema

✅ **Actualizar Rol:**

- No se pueden modificar `ROLE_ADMIN` ni `ROLE_USER`
- Validación de nombre único

✅ **Eliminar Rol:**

- No se pueden eliminar roles del sistema
- No se puede eliminar si hay usuarios asignados

### **Ejemplo de Creación**

**Request:**

```json
POST /roles
Authorization: Bearer <token-admin>

{
  "name": "ROLE_VENDEDOR",
  "description": "Rol para vendedores del sistema"
}
```

**Response:**

```json
{
  "id": 4,
  "name": "ROLE_VENDEDOR",
  "description": "Rol para vendedores del sistema",
  "userCount": 0
}
```

---

## 💳 Módulo de Pagos (payment)

### **Payment.java**

```json
{
  "id": 1,
  "userId": 1,
  "amount": 59990,
  "paymentMethod": "CREDIT_CARD",
  "cardType": "VISA",
  "status": "COMPLETED",
  "products": [{ "productId": 1, "quantity": 2 }],
  "transactionId": "TXN123456",
  "createdAt": "2025-11-13T18:00:00"
}
```

### **Endpoints de Pagos**

| Método | Endpoint                 | Descripción                    | Auth          |
| ------ | ------------------------ | ------------------------------ | ------------- |
| POST   | `/pagos`                 | Iniciar pago                   | ✅ USER       |
| POST   | `/pagos/{id}/confirmar`  | Confirmar pago                 | ✅ USER       |
| GET    | `/pagos/mis-pagos`       | Historial de pagos del usuario | ✅ USER       |
| GET    | `/pagos/{id}`            | Obtener pago por ID            | ✅ USER/ADMIN |
| GET    | `/pagos`                 | Listar todos los pagos         | ✅ ADMIN      |
| POST   | `/pagos/{id}/reembolsar` | Reembolsar pago                | ✅ ADMIN      |

### **Estados de Pago**

- `PENDING` - Pago iniciado, esperando confirmación
- `COMPLETED` - Pago completado exitosamente
- `FAILED` - Pago fallido
- `REFUNDED` - Pago reembolsado

### **Flujo de Pago**

1. **Iniciar Pago**: Usuario crea un pago con productos
2. **Validación**: Sistema valida stock y calcula total
3. **Confirmación**: Usuario confirma el pago
4. **Procesamiento**: Sistema actualiza stock y genera transacción
5. **Completado**: Pago registrado exitosamente

### **Token de Pago**

Flujo de pago seguro con token temporal:

1. Usuario autenticado solicita token de pago
2. Backend genera `PAYMENT_TOKEN` con duración corta
3. Frontend usa este token para procesar pago
4. Backend valida token + usuario + productos
5. Procesa pago y actualiza stock

---

## 📊 Módulo de Auditoría (audit) 🆕

### **Sistema de Auditoría Completo**

Registra todas las operaciones realizadas en el sistema usando **AOP (Aspect Oriented Programming)**.

### **AuditLog.java**

```json
{
  "id": 1,
  "username": "admin",
  "roles": "ROLE_ADMIN",
  "httpMethod": "POST",
  "path": "/productos",
  "action": "ProductController#createProduct",
  "arguments": "{\"name\":\"Catan\",\"price\":29990}",
  "success": true,
  "details": null,
  "timestamp": "2025-11-17T15:30:00"
}
```

### **Características**

✅ **Intercepta automáticamente** todas las operaciones en controllers
✅ **Registra usuario y roles** de quien ejecuta la operación
✅ **Captura argumentos** de los métodos (JSON)
✅ **Registra éxito o error** con detalles de excepciones
✅ **Timestamp** de cada operación

### **Endpoints de Auditoría**

| Método | Endpoint                      | Descripción                   | Auth     |
| ------ | ----------------------------- | ----------------------------- | -------- |
| GET    | `/audit/logs`                 | Listar todos los logs         | ✅ ADMIN |
| GET    | `/audit/logs/user/{username}` | Logs de un usuario específico | ✅ ADMIN |
| GET    | `/audit/logs/failed`          | Solo operaciones fallidas     | ✅ ADMIN |
| GET    | `/audit/logs/recent`          | Últimos 100 registros         | ✅ ADMIN |

### **AuditAspect.java**

```java
@Around("within(com.ecommerce.levelup..controller..*)")
public Object aroundController(ProceedingJoinPoint pjp) throws Throwable {
    // Intercepta TODOS los métodos de controllers
    // Registra antes y después de la ejecución
    // Captura excepciones y las registra
}
```

### **Casos de Uso**

- 🔍 **Debugging**: Ver qué operaciones fallan y por qué
- 🔒 **Seguridad**: Detectar accesos no autorizados
- 📈 **Análisis**: Estadísticas de uso del sistema
- 🕵️ **Trazabilidad**: Saber quién hizo qué y cuándo

---

## Módulo de Configuración (config)

### **CorsConfig.java**

Permite requests desde frontend React:

```java
setAllowedOrigins("http://localhost:5173", "http://localhost:3000")
setAllowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
setAllowedHeaders("*")
setAllowCredentials(false)  // Importante para evitar 403
```

### **DataInitializer.java**

Ejecuta al iniciar la aplicación:

```java
@Component
public class DataInitializer implements CommandLineRunner {
    // Crea roles: ROLE_ADMIN, ROLE_USER, ROLE_MODERATOR
    // Crea usuario admin: username=admin, password=admin123
    // Crea usuario regular: username=user, password=user123
}
```

### **GlobalExceptionHandler.java**

Manejo centralizado de errores con `@RestControllerAdvice`:

- `MethodArgumentNotValidException`: Errores de validación (400)
- `BadCredentialsException`: Credenciales incorrectas (401)
- `AccessDeniedException`: Acceso denegado (403)
- `RuntimeException`: Errores de negocio (400)
- `Exception`: Errores internos (500)

**Response estándar:**

```json
{
  "timestamp": "2025-11-13T18:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "details": "..."
}
```

**Códigos de Estado HTTP:**

- `200 OK`: Operación exitosa
- `201 Created`: Recurso creado exitosamente
- `400 Bad Request`: Error de validación o negocio
- `401 Unauthorized`: No autenticado o token inválido
- `403 Forbidden`: No tiene permisos suficientes
- `404 Not Found`: Recurso no encontrado
- `500 Internal Server Error`: Error del servidor

---

## 🔄 Características Técnicas Avanzadas

### **Gestión de Stock Automática**

- El sistema actualiza el stock automáticamente al confirmar un pago
- Método `decreaseStock()` en la entidad Product valida disponibilidad
- Transacciones garantizan integridad (si falla el pago, no se reduce stock)
- Endpoint específico `PATCH /productos/{id}/stock` para ajustes manuales

### **Generación de Códigos**

- **SKU de Productos**: Formato `CATEGORIA###` (ej: JM001, JM002)
- **Códigos de Categoría**: Definidos manualmente al crear (ej: JM, AC, EL)
- **IDs de Transacción**: UUID únicos para cada pago
- Algoritmo busca el último código y genera el siguiente secuencialmente

### **Auditoría AOP**

```java
@Around("within(com.ecommerce.levelup..controller..*)")
```

- Intercepta **todos** los métodos de controladores
- Captura automáticamente: usuario, roles, método HTTP, path, argumentos
- Registra éxito/error con detalles de excepción
- Performance: mínimo overhead (~1-2ms por request)

### **Seguridad en Capas**

1. **Nivel de Red**: CORS configurado
2. **Nivel de Filtro**: JwtFilter valida tokens
3. **Nivel de Método**: `@PreAuthorize` en cada endpoint
4. **Nivel de Datos**: Validaciones con Jakarta Validation
5. **Nivel de Negocio**: Lógica adicional en servicios

### **Optimizaciones de Base de Datos**

- Índices únicos en `username`, `email`, `code`, `transactionId`
- JOIN FETCH para evitar N+1 queries en relaciones
- `@Transactional` para operaciones atómicas
- Lazy loading configurado estratégicamente
- Timestamps automáticos con `@CreationTimestamp` y `@UpdateTimestamp`

---

## 📦 DTOs (Data Transfer Objects)

### **¿Por qué DTOs?**

1. **Seguridad**: No exponer campos sensibles (password)
2. **Flexibilidad**: Estructura de respuesta diferente al modelo
3. **Validación**: Anotaciones `@NotBlank`, `@NotNull`, `@Email`
4. **Desacoplamiento**: Cambios en DB no afectan API
5. **Campos Calculados**: Agregar datos derivados (ej: fullName, userCount)

### **DTOs Principales**

#### **LoginResponse.java**

```json
{
  "token": "eyJ...",
  "type": "Bearer",
  "username": "admin",
  "email": "admin@ecommerce.com",
  "roles": ["ROLE_ADMIN"]
}
```

#### **ProductDTO.java**

```json
{
  "id": 1,
  "code": "JM001",
  "name": "Catan",
  "price": 29990,
  "categoryId": 1,
  "category": "Juegos de Mesa",
  "image": "https://...",
  "description": "...",
  "stock": 10,
  "featured": false
}
```

#### **CategoryDTO.java**

```json
{
  "id": 1,
  "name": "Juegos de Mesa",
  "code": "JM",
  "description": "...",
  "active": true
}
```

---

## Base de Datos

### **Configuración (application.properties)**

```properties
# MySQL (Laragon)
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
jwt.secret=miSuperSecretoSeguroQueDebeSerMuyLargoYComplejo123456789
jwt.expiration=86400000  # 24 horas
```

### **Tablas Generadas**

```sql
users          -- Usuarios del sistema
roles          -- Roles (ADMIN, USER, etc.)
user_roles     -- Relación ManyToMany usuarios-roles
categories     -- Categorías de productos
products       -- Productos del catálogo
payments       -- Pagos y transacciones
audit_logs     -- Registros de auditoría (nuevo)
```

### **Esquema de Entidades**

| Entidad      | Campos Principales                                                                    | Relaciones           |
| ------------ | ------------------------------------------------------------------------------------- | -------------------- |
| **User**     | id, username, email, password, firstName, lastName, phone, address, region, city      | ManyToMany → Role    |
| **Role**     | id, name, description                                                                 | ManyToMany → User    |
| **Category** | id, name, code, description, active                                                   | OneToMany → Product  |
| **Product**  | id, code, name, price, stock, image, description, featured, active                    | ManyToOne → Category |
| **Payment**  | id, userId, totalAmount, paymentMethod, status, transactionId, paymentToken, products | -                    |
| **AuditLog** | id, username, roles, httpMethod, path, action, arguments, success, timestamp          | -                    |

---

## Instalación y Ejecución

### **Prerrequisitos**

- Java 17
- Maven 3.9.11
- MySQL (Laragon)
- Postman (para testing)

### **Pasos**

1. **Clonar repositorio**

```bash
cd C:\Users\FullHackerMIX\OneDrive\Desktop\Projects\ecommerce-backend
```

2. **Crear base de datos**

```sql
CREATE DATABASE ecommerce_db;
```

3. **Compilar proyecto**

```bash
mvn clean install -DskipTests
```

4. **Ejecutar aplicación**

```bash
mvn spring-boot:run
```

5. **Verificar**

```
http://localhost:8080
```

---

## 🧪 Testing con Postman

### **1. Login como Admin**

```
POST http://localhost:8080/autenticacion/login
Body:
{
  "username": "admin",
  "password": "admin123"
}

Response → Copiar token
```

### **2. Crear Categoría**

```
POST http://localhost:8080/categorias
Headers:
  Authorization: Bearer <token>
Body:
{
  "name": "Juegos de Mesa",
  "code": "JM",
  "description": "Juegos de estrategia"
}
```

### **3. Crear Producto**

```
POST http://localhost:8080/productos
Headers:
  Authorization: Bearer <token>
Body:
{
  "name": "Catan",
  "price": 29990,
  "categoryId": 1,
  "image": "https://i.imgur.com/Azw0XR8.jpeg",
  "description": "Juego de estrategia",
  "stock": 10
}

Response → Code generado automáticamente: "JM001"
```

### **4. Listar Productos (Sin token)**

```
GET http://localhost:8080/productos
```

### **5. Crear Rol Personalizado**

```
POST http://localhost:8080/roles
Headers:
  Authorization: Bearer <token-admin>
Body:
{
  "name": "ROLE_VENDEDOR",
  "description": "Rol para vendedores"
}
```

### **6. Cambiar Contraseña**

```
POST http://localhost:8080/autenticacion/cambiar-contrasena
Headers:
  Authorization: Bearer <token>
Body:
{
  "currentPassword": "admin123",
  "newPassword": "nuevaPass123",
  "confirmPassword": "nuevaPass123"
}
```

### **7. Ver Logs de Auditoría**

```
GET http://localhost:8080/audit/logs
Headers:
  Authorization: Bearer <token-admin>
```

### **8. Procesar Pago**

```
POST http://localhost:8080/pagos
Headers:
  Authorization: Bearer <token>
Body:
{
  "totalAmount": 59990,
  "paymentMethod": "CREDIT_CARD",
  "cardType": "VISA",
  "products": [
    {
      "productId": 1,
      "quantity": 2
    }
  ]
}
```

### **9. Actualizar Usuario**

```
PUT http://localhost:8080/usuarios/1
Headers:
  Authorization: Bearer <token-admin>
Body:
{
  "firstName": "Juan",
  "lastName": "Pérez",
  "phone": "987654321",
  "address": "Av. Principal 456",
  "region": "Valparaíso",
  "city": "Viña del Mar"
}
```

### **10. Actualizar Stock de Producto**

```
PATCH http://localhost:8080/productos/1/stock?cantidad=50
Headers:
  Authorization: Bearer <token-admin>
```

---

## 🔧 Endpoints de Debug (Temporal)

| Endpoint                  | Descripción                        |
| ------------------------- | ---------------------------------- |
| POST `/debug/reset-admin` | Resetea password del admin         |
| GET `/debug/admin-info`   | Info del usuario admin             |
| GET `/debug/all-users`    | Lista todos los usuarios con roles |

**⚠️ Eliminar en producción**

---

## 🐛 Problemas Resueltos

### **1. Roles vacíos en LoginResponse**

**Problema**: `"roles": []` aunque existen en DB

**Causa**: Lazy loading + transacción cerrada

**Solución**:

- `@Query` con `JOIN FETCH` en `UserRepository`
- `@Transactional(readOnly = true)` en `AuthService.login()`
- `user.getRoles().size()` para forzar carga

### **2. StackOverflowError en User-Role**

**Problema**: Ciclo infinito al serializar JSON

**Causa**: Relación bidireccional sin anotaciones

**Solución**:

- `@JsonManagedReference` en User.roles
- `@JsonBackReference` + `@JsonIgnore` en Role.users
- `@EqualsAndHashCode(exclude = "roles")` en ambos

### **3. CORS 403 Forbidden**

**Problema**: Frontend no puede hacer requests

**Causa**: `setAllowCredentials(true)` con orígenes específicos

**Solución**:

- `setAllowCredentials(false)` en `CorsConfig`
- Remover `@CrossOrigin` de controllers

### **4. Maven no reconocido en Windows**

**Problema**: PATH con locales español

**Solución**:

```
C:\Archivos de programa\Apache\maven\apache-maven-3.9.11\bin
```

### **5. Payment Token demasiado largo**

**Problema**: Error al crear pago - "Data too long for column 'payment_token'"

**Causa**: Campo `payment_token` con longitud de 100, pero JWT necesita ~1000 caracteres

**Solución**:

```sql
ALTER TABLE payments MODIFY COLUMN payment_token VARCHAR(1000);
```

O borrar la tabla y reiniciar para que Hibernate la recree con el tamaño correcto.

---

## 📝 Convenciones del Proyecto

### **Nomenclatura**

- **Controllers**: `XxxController.java`
- **Services**: `XxxService.java`
- **Repositories**: `XxxRepository.java`
- **DTOs**: `XxxDTO.java`, `XxxRequest.java`, `XxxResponse.java`
- **Models/Entities**: `Xxx.java` (sin sufijo)

### **Estructura de Paquetes**

```
feature/
  ├── controller/    # REST endpoints
  ├── service/       # Lógica de negocio
  ├── repository/    # Acceso a datos
  ├── model/         # Entidades JPA
  └── dto/           # Data Transfer Objects
```

### **Anotaciones Comunes**

- `@RestController` + `@RequestMapping`: Controllers
- `@Service`: Servicios
- `@Repository`: Repositorios
- `@Entity` + `@Table`: Entidades
- `@Data` + `@NoArgsConstructor`: Lombok
- `@PreAuthorize`: Seguridad a nivel de método
- `@Transactional`: Control de transacciones
- `@Valid`: Validación automática de DTOs

---

## 📋 Reglas de Negocio Implementadas

### **Usuarios**

- ✅ Username único (3-50 caracteres)
- ✅ Email único y formato válido
- ✅ Password mínimo 6 caracteres (encriptado con BCrypt)
- ✅ No se puede eliminar el último admin del sistema
- ✅ Soft delete con campo `enabled`
- ✅ Auditoría de creación y actualización (timestamps)

### **Roles**

- ✅ Nombre debe empezar con `ROLE_`
- ✅ Solo mayúsculas y guiones bajos permitidos
- ✅ Roles del sistema (ADMIN, USER) no se pueden modificar/eliminar
- ✅ No se puede eliminar rol con usuarios asignados
- ✅ Contador de usuarios por rol (userCount)

### **Productos**

- ✅ SKU único generado automáticamente
- ✅ Precio debe ser mayor a 0
- ✅ Stock no puede ser negativo
- ✅ Debe pertenecer a una categoría válida
- ✅ Soft delete con campo `active`
- ✅ Validación de disponibilidad antes de vender

### **Categorías**

- ✅ Código único definido manualmente
- ✅ No se puede eliminar categoría con productos
- ✅ Soft delete con campo `active`
- ✅ Generación automática de siguiente SKU para productos

### **Pagos**

- ✅ Monto total debe ser mayor a 0
- ✅ Debe incluir al menos un producto
- ✅ Validación de stock antes de procesar
- ✅ Actualización automática de stock al confirmar
- ✅ Estados: PENDING → COMPLETED/FAILED/REFUNDED
- ✅ Reembolsos solo para pagos completados
- ✅ Token de pago temporal para seguridad

### **Auditoría**

- ✅ Registro automático de todas las operaciones
- ✅ Captura de usuario autenticado y roles
- ✅ Almacenamiento de argumentos del método
- ✅ Registro de éxito/error con detalles
- ✅ Filtrado por usuario, fecha, estado
- ✅ Solo administradores pueden ver logs

---

## 🎯 Funcionalidades Implementadas

- ✅ Autenticación y autorización con JWT
- ✅ Gestión completa de usuarios (CRUD)
- ✅ Gestión completa de roles (CRUD) 🆕
- ✅ Cambio de contraseña 🆕
- ✅ Gestión de productos y categorías
- ✅ Generación automática de SKU
- ✅ Sistema de pagos con validación
- ✅ Reembolsos de pagos
- ✅ Auditoría completa con AOP 🆕
- ✅ Gestión de stock (actualización individual y por venta)
- ✅ Roles y permisos (ADMIN, USER)
- ✅ Validaciones de negocio
- ✅ Manejo centralizado de excepciones
- ✅ CORS configurado para frontend

## 🎯 Próximos Pasos

- [ ] Implementar paginación en listados
- [ ] Agregar filtros avanzados (precio, stock, categorías)
- [ ] Sistema de imágenes (upload a servidor/S3)
- [ ] Carrito de compras persistente
- [ ] Órdenes/Pedidos con estados (PENDING, SHIPPED, DELIVERED)
- [ ] Integración con pasarelas de pago reales (Stripe, PayPal)
- [ ] Notificaciones por email (confirmación, envíos)
- [ ] Dashboard de administración con estadísticas
- [ ] Sistema de reviews y ratings
- [ ] Búsqueda avanzada con Elasticsearch
- [ ] Tests unitarios y de integración (JUnit, Mockito)
- [ ] Documentación con Swagger/OpenAPI
- [ ] Caché con Redis
- [ ] Logs con ELK Stack

---

## 👥 Credenciales de Prueba

| Usuario | Contraseña | Rol        |
| ------- | ---------- | ---------- |
| admin   | admin123   | ROLE_ADMIN |
| user    | user123    | ROLE_USER  |

---

## 📋 Referencia Completa de Endpoints

### **Autenticación (`/autenticacion`)**

| Método | Endpoint              | Auth | Descripción             |
| ------ | --------------------- | ---- | ----------------------- |
| POST   | `/registrar`          | ❌   | Registrar nuevo usuario |
| POST   | `/login`              | ❌   | Iniciar sesión          |
| POST   | `/refrescar`          | ✅   | Refrescar token JWT     |
| GET    | `/validar`            | ✅   | Validar token           |
| GET    | `/yo`                 | ✅   | Obtener usuario actual  |
| POST   | `/cambiar-contrasena` | ✅   | Cambiar contraseña 🆕   |

### **Usuarios (`/usuarios`)**

| Método | Endpoint       | Auth        | Descripción        |
| ------ | -------------- | ----------- | ------------------ |
| GET    | `/`            | ADMIN       | Listar usuarios    |
| GET    | `/{id}`        | ADMIN       | Obtener usuario    |
| PUT    | `/{id}`        | ADMIN/Owner | Actualizar usuario |
| PATCH  | `/{id}/estado` | ADMIN       | Activar/Desactivar |
| DELETE | `/{id}`        | ADMIN       | Eliminar usuario   |

### **Roles (`/roles`)** 🆕

| Método | Endpoint | Auth  | Descripción    |
| ------ | -------- | ----- | -------------- |
| GET    | `/`      | ADMIN | Listar roles   |
| GET    | `/{id}`  | ADMIN | Obtener rol    |
| POST   | `/`      | ADMIN | Crear rol      |
| PUT    | `/{id}`  | ADMIN | Actualizar rol |
| DELETE | `/{id}`  | ADMIN | Eliminar rol   |

### **Productos (`/productos`)**

| Método | Endpoint          | Auth  | Descripción                 |
| ------ | ----------------- | ----- | --------------------------- |
| GET    | `/`               | ❌    | Listar productos            |
| GET    | `/{id}`           | ❌    | Obtener producto            |
| GET    | `/categoria/{id}` | ❌    | Productos por categoría     |
| GET    | `/buscar`         | ❌    | Buscar productos            |
| GET    | `/activos`        | ❌    | Solo productos activos      |
| POST   | `/`               | ADMIN | Crear producto (genera SKU) |
| PUT    | `/{id}`           | ADMIN | Actualizar producto         |
| PATCH  | `/{id}/stock`     | ADMIN | Actualizar solo el stock    |
| DELETE | `/{id}`           | ADMIN | Eliminar producto           |

### **Categorías (`/categorias`)**

| Método | Endpoint   | Auth  | Descripción                     |
| ------ | ---------- | ----- | ------------------------------- |
| GET    | `/`        | ❌    | Listar categorías               |
| GET    | `/{id}`    | ❌    | Obtener categoría               |
| GET    | `/activas` | ❌    | Solo categorías activas         |
| POST   | `/`        | ADMIN | Crear categoría (genera código) |
| PUT    | `/{id}`    | ADMIN | Actualizar categoría            |
| DELETE | `/{id}`    | ADMIN | Eliminar categoría              |

### **Pagos (`/pagos`)**

| Método | Endpoint           | Auth       | Descripción            |
| ------ | ------------------ | ---------- | ---------------------- |
| POST   | `/`                | USER       | Iniciar pago           |
| POST   | `/{id}/confirmar`  | USER       | Confirmar pago         |
| GET    | `/mis-pagos`       | USER       | Historial del usuario  |
| GET    | `/{id}`            | USER/ADMIN | Obtener pago           |
| GET    | `/`                | ADMIN      | Listar todos los pagos |
| POST   | `/{id}/reembolsar` | ADMIN      | Reembolsar pago        |

### **Auditoría (`/audit`)** 🆕

| Método | Endpoint                | Auth  | Descripción               |
| ------ | ----------------------- | ----- | ------------------------- |
| GET    | `/logs`                 | ADMIN | Todos los logs            |
| GET    | `/logs/user/{username}` | ADMIN | Logs de un usuario        |
| GET    | `/logs/failed`          | ADMIN | Solo operaciones fallidas |
| GET    | `/logs/recent`          | ADMIN | Últimos 100 registros     |

---

## 📖 Documentación Swagger/OpenAPI 🆕

El proyecto incluye documentación interactiva completa de todos los endpoints mediante **Springdoc OpenAPI**.

### **Acceder a Swagger UI**

Una vez iniciado el servidor, accede a:

```
http://localhost:8080/swagger-ui.html
```

O directamente:

```
http://localhost:8080/swagger-ui/index.html
```

### **OpenAPI JSON/YAML**

Descarga la especificación OpenAPI en:

```
http://localhost:8080/v3/api-docs          # JSON
http://localhost:8080/v3/api-docs.yaml     # YAML
```

### **Características de la Documentación**

✅ **Todos los controladores documentados**:

- 🔐 **Autenticación**: Login, registro, cambio de contraseña, validación
- 🛍️ **Productos**: CRUD completo, filtros, actualización de stock
- 📂 **Categorías**: Gestión de categorías activas e inactivas
- 👥 **Usuarios**: Administración de usuarios y activación/desactivación
- 🎭 **Roles**: CRUD de roles con protección de roles del sistema
- 💳 **Pagos**: Iniciar, confirmar, consultar y reembolsar pagos
- 📊 **Auditoría**: Consultar logs con filtros avanzados
- 🐛 **Debug**: Herramientas de desarrollo (solo perfil dev)

✅ **Seguridad JWT integrada**:

- Configuración de esquema Bearer Authentication
- Botón "Authorize" para probar endpoints protegidos
- Indicadores visuales de endpoints que requieren autenticación

✅ **Descripciones detalladas**:

- Resumen y descripción de cada endpoint
- Parámetros documentados con tipos y validaciones
- Códigos de respuesta HTTP (200, 400, 404, etc.)
- Ejemplos de request/response

### **Uso de Swagger UI**

1. **Autenticarse**:

   - Haz login en `/autenticacion/login` para obtener el token JWT
   - Copia el token (sin "Bearer ")
   - Clic en el botón **"Authorize"** (candado verde)
   - Pega el token y confirma
   - Ahora puedes probar endpoints protegidos

2. **Probar endpoints**:

   - Expande cualquier endpoint
   - Clic en **"Try it out"**
   - Completa los parámetros necesarios
   - Clic en **"Execute"**
   - Ver la respuesta en tiempo real

3. **Ver modelos de datos**:
   - Al final de cada grupo, ver "Schemas"
   - Muestra la estructura de DTOs y entidades

### **Configuración del Proyecto**

La configuración de Swagger está en `SwaggerConfig.java`:

```java
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("E-Commerce Backend API")
                .version("1.0")
                .description("API RESTful completa..."))
            .addSecurityItem(new SecurityRequirement()
                .addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
```

**Nota**: Los endpoints de Swagger están whitelistados en `SecurityConfig` para acceso sin autenticación.

---

## 🌐 Integración Frontend - Backend

### **Arquitectura de Comunicación**

El backend funciona como API REST stateless que se comunica con el frontend (React, Angular, Vue, etc.) mediante HTTP/HTTPS con formato JSON.

```
┌─────────────────┐         HTTP/HTTPS          ┌─────────────────┐
│                 │    ← JSON Request/Response → │                 │
│   FRONTEND      │                              │    BACKEND      │
│  (React/Vue)    │         REST API             │  (Spring Boot)  │
│                 │                              │                 │
└─────────────────┘                              └─────────────────┘
       │                                                 │
       │ localStorage/sessionStorage                     │
       │ (Guarda JWT token)                     ┌───────┴────────┐
       │                                        │   MySQL DB      │
       │                                        │   (Laragon)     │
       └────────────────────────────────────────│                 │
              Token en cada request              └─────────────────┘
```

### **Configuración CORS**

El backend tiene CORS configurado en `CorsConfig.java` para permitir requests desde el frontend:

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:3000", "http://localhost:5173")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
```

**Puertos comunes de frontend**:

- React (Create React App): `http://localhost:3000`
- Vite (React/Vue): `http://localhost:5173`
- Angular: `http://localhost:4200`

### **Flujo de Autenticación Frontend-Backend**

#### **1. Login del Usuario**

```javascript
// Frontend - Ejemplo con Fetch API
async function login(username, password) {
  const response = await fetch("http://localhost:8080/autenticacion/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });

  const data = await response.json();

  if (response.ok) {
    // Guardar token en localStorage
    localStorage.setItem("token", data.token);
    localStorage.setItem("username", data.username);
    localStorage.setItem("roles", JSON.stringify(data.roles));

    return data;
  } else {
    throw new Error(data.error || "Login fallido");
  }
}
```

#### **2. Realizar Requests Autenticados**

```javascript
// Frontend - Agregar token en headers
async function getUserProfile() {
  const token = localStorage.getItem("token");

  const response = await fetch("http://localhost:8080/autenticacion/yo", {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  });

  return await response.json();
}
```

#### **3. Interceptor Global (Axios)**

```javascript
// Frontend - Configurar Axios con interceptor
import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080",
  headers: { "Content-Type": "application/json" },
});

// Agregar token automáticamente a cada request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Manejo de errores 401 (token expirado)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Redirigir a login
      localStorage.clear();
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export default api;
```

### **Endpoints Públicos vs Protegidos**

#### **Públicos (sin token)**

```javascript
// Listar productos
GET http://localhost:8080/productos

// Obtener producto por ID
GET http://localhost:8080/productos/1

// Listar categorías activas
GET http://localhost:8080/categorias/activas

// Login
POST http://localhost:8080/autenticacion/login

// Registro
POST http://localhost:8080/autenticacion/registrar
```

#### **Protegidos (requieren token)**

```javascript
// Obtener perfil del usuario actual
GET http://localhost:8080/autenticacion/yo
Authorization: Bearer <token>

// Mis pagos
GET http://localhost:8080/pagos/mis-pagos
Authorization: Bearer <token>

// Cambiar contraseña
POST http://localhost:8080/autenticacion/cambiar-contrasena
Authorization: Bearer <token>
```

#### **Solo ADMIN (requieren token + ROLE_ADMIN)**

```javascript
// Crear producto
POST http://localhost:8080/productos
Authorization: Bearer <token>

// Gestionar usuarios
GET http://localhost:8080/usuarios
Authorization: Bearer <token>

// Ver logs de auditoría
GET http://localhost:8080/audit/logs
Authorization: Bearer <token>
```

### **Manejo de Roles en Frontend**

```javascript
// Verificar si usuario es admin
function isAdmin() {
  const roles = JSON.parse(localStorage.getItem("roles") || "[]");
  return roles.includes("ROLE_ADMIN");
}

// Renderizado condicional en React
function AdminPanel() {
  if (!isAdmin()) {
    return <Navigate to="/unauthorized" />;
  }

  return (
    <div>
      <h1>Panel de Administración</h1>
      {/* Contenido solo para admins */}
    </div>
  );
}
```

### **Ejemplo Completo: Crear Producto**

```javascript
// Frontend - Formulario de producto
async function createProduct(productData) {
  const token = localStorage.getItem("token");

  const response = await fetch("http://localhost:8080/productos", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      name: productData.name,
      description: productData.description,
      price: productData.price,
      stock: productData.stock,
      imageUrl: productData.imageUrl,
      categoryId: productData.categoryId,
      active: true,
    }),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error || "Error al crear producto");
  }

  return await response.json();
}
```

### **Variables de Entorno Frontend**

Crear archivo `.env` en el proyecto frontend:

```env
# React
REACT_APP_API_URL=http://localhost:8080

# Vite
VITE_API_URL=http://localhost:8080

# Angular
NG_APP_API_URL=http://localhost:8080
```

Uso:

```javascript
// React
const API_URL = process.env.REACT_APP_API_URL;

// Vite
const API_URL = import.meta.env.VITE_API_URL;
```

### **Estructura Recomendada Frontend**

```
frontend/
├── src/
│   ├── api/
│   │   ├── axios.js           # Configuración de Axios
│   │   ├── authApi.js         # Endpoints de autenticación
│   │   ├── productApi.js      # Endpoints de productos
│   │   ├── userApi.js         # Endpoints de usuarios
│   │   └── paymentApi.js      # Endpoints de pagos
│   │
│   ├── contexts/
│   │   └── AuthContext.jsx    # Context de autenticación
│   │
│   ├── components/
│   │   ├── ProtectedRoute.jsx # HOC para rutas protegidas
│   │   └── AdminRoute.jsx     # HOC para rutas de admin
│   │
│   └── pages/
│       ├── Login.jsx
│       ├── Products.jsx
│       └── AdminDashboard.jsx
```

### **Testing de Integración**

Herramientas para probar la API:

1. **Swagger UI**: `http://localhost:8080/swagger-ui.html` ✅ Recomendado
2. **Postman**: Importar colección desde OpenAPI JSON
3. **Thunder Client**: Extensión de VS Code
4. **cURL**: Comandos en terminal

Ejemplo cURL:

```bash
# Login
curl -X POST http://localhost:8080/autenticacion/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Obtener productos (con token)
curl -X GET http://localhost:8080/productos \
  -H "Authorization: Bearer <token>"
```

---

## 🔧 Integración Backend - Backend (Microservicios)

Si deseas integrar este backend con otros servicios:

### **Como API Gateway**

```java
// Agregar dependencia Spring Cloud Gateway en otro proyecto
implementation 'org.springframework.cloud:spring-cloud-starter-gateway'

// Configurar rutas
spring:
  cloud:
    gateway:
      routes:
        - id: ecommerce-backend
          uri: http://localhost:8080
          predicates:
            - Path=/productos/**,/categorias/**,/pagos/**
```

### **Comunicación entre servicios**

```java
// Usar RestTemplate o WebClient para llamar a este backend
@Service
public class ExternalService {

    private final RestTemplate restTemplate;

    public ProductDTO getProduct(Long id) {
        String url = "http://localhost:8080/productos/" + id;
        return restTemplate.getForObject(url, ProductDTO.class);
    }
}
```

### **Service Discovery (Eureka)**

```yaml
# application.yml del microservicio
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    instance-id: ${spring.application.name}:${random.value}
```

---

## 📚 Referencias

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security + JWT](https://spring.io/guides/topicals/spring-security-architecture)
- [Spring AOP](https://docs.spring.io/spring-framework/reference/core/aop.html)
- [Springdoc OpenAPI](https://springdoc.org/)
- [JJWT Documentation](https://github.com/jwtk/jjwt)
- [Lombok](https://projectlombok.org/)

---

## 📄 Licencia

Este proyecto es educativo y no tiene licencia comercial.

---

## ✨ Autor

Desarrollado como proyecto de aprendizaje de Spring Boot + React.

**Fecha**: Noviembre 2025

---

## 🌟 Características Destacadas

### **Seguridad**

- 🔐 Autenticación JWT con tokens seguros (HS384)
- 🛡️ Roles y permisos granulares
- 🔑 Cambio de contraseña validado
- 🚫 Protección contra accesos no autorizados
- ✅ Validaciones en backend

### **Arquitectura**

- 📦 Arquitectura de capas (Controller-Service-Repository)
- 🎯 DTOs para transferencia de datos segura
- ⚡ Transacciones con `@Transactional`
- 🔄 Lazy loading optimizado con JOIN FETCH
- 🎨 AOP para auditoría automática

### **Gestión de Datos**

- 🏷️ Generación automática de SKU para productos
- 🔢 Códigos automáticos para categorías
- 📊 Validaciones de negocio completas
- 🔄 Relaciones ManyToMany optimizadas
- 💾 Soft delete (usuarios y productos)
- 📦 Gestión de stock automática en pagos
- 📍 Información de ubicación (región y ciudad)

### **Auditoría y Trazabilidad**

- 📝 Logs automáticos de todas las operaciones
- 👤 Registro de usuario y roles en cada acción
- ⏰ Timestamps de todas las operaciones
- ❌ Captura de errores con stack trace
- 🔍 Consultas filtradas por usuario o estado

### **API RESTful**

- 🌐 Endpoints RESTful bien estructurados
- 📋 Respuestas JSON estándar
- ❗ Manejo centralizado de excepciones
- 🔧 CORS configurado para frontend
- 📖 Documentación completa en README

---
