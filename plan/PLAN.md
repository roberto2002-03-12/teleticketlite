## Description of modules

### users
#### Purpose:
Manage own user's profile, cannot modify other users profile, only it's own. Can create new users and register them in aws cognito with the role "CLIENT" as default, it also should register the user in the database with same default role "CLIENT".

When an user is created and saved in the database, it should also register in AWS cognito. If the user's registration fails when inserting into database or registering to AWS cognito, then it should be deleted to avoid future conflicts, use "transactional" so you can rollback when inserting into the database when an error occours, in the case of AWS cognito, you should delete the account manually via aws sdk that the project has.

NOTE: email cannot be the same as other's user, it's an unique value, sames as DNI and cellphone number.
#### use of cases
- edit their own personal information (needs token)
- see their own personal information (needs token)
- create an account <- should register user to database and aws cognito (no needed)
- create an account with role "STAFF" and affiliate the user with a given event_id, it uses "users" and "staff" table (needs token and only users with role "OWNER" can do it)
- desaffiliate staff user from an event by the given "event_id", this involves delete register from table "staff" where user_id matchs with user's id and event_id matchs with the given event_id
- create an account with role "OWNER", only users with role "ADMIN" can do such action. (requires token)
- delete an account, only users with role "ADMIN" can do such action. (requires token)

#### note:
users could upload or change their profile picture, or just delete it.

### auth
#### Purpose:
It is responsible for exposing middlewares or functions of type "x" to be used in endpoints to validate the received token in the request header, as well as authorization strictly based on the role defined in the token's payload. This module does not handle login or user registration. It only handles token validation and user authorization through the credentials provided by AWS Cognito. It should not make requests to the Cognito API to validate, but rather use the credentials provided by Cognito in order to validate the tokens from request header, since sending to validate with AWS Cognito API would be expensive.

This module must serve to expose its functions to the controllers or services (as deemed necessary) of other modules.

NOTE: If Quarkus with a specific AWS SDK already provides these functions, omit the creation of these validation functions and/or annotations and use them directly; however, it must comply with the requested requirements.

#### Use of cases
- Validation of requests with the token in the header.
- Validation of authorization with the assigned role via the payload of the token provided in the header.

### events
#### Purpose:
Este modulo se encarga de la gestión de las tablas "event", "event_category" y "event_images".

El proposito de "event" es ser la tabla en la cual se registra un evento en los cuales el usuario con rol "CLIENT" puede inscribirse al evento a través de la tabla "qr_ticket" (se desarrollara más adelante, no en este modulo). El usuario que crea el evento (rol: "OWNER" y "ADMIN") pueden agregar varias imagenes hasta 8, estas imagenes tienen un orden, si se llega 8 imagenes en el formulario, el primero tendrá el index 0 hasta llegar a 7 según el orden de las imagenes llegadas. El usuario también puede asignar categoria al evento, siendo solamente una.

La creación de un evento se puede definir lo siguiente:
- title
- description
- max_people
- address
- available
- finished
- start_date
- finish_date
- owner_id (se obtiene el id del usuario que realiza la petición, detalles más adelante)
- category_id (id de "event_category" existente)

Para poder ver el tipo de atributo, revisa `plan/ARCHITECTURE.MD`

¿Cómo obtener el "owner_id"?
Mediante el token obtenido obtienes el email del usuario que esta logueado, con ese email obtienes el id del usuario, luego de obtener el id de usuario debes crear un método para obtener el id_event_onwer de la tabla "event_owner" buscando mediante "user_id" con el id de usuario obtenido mediante el email del token.

#### Use of cases
- crear evento (SOLO)

Los usuarios con el rol "CLIENT" solo pueden leer eventos que esten activos, los de rol "OWNER" y "STAFF" solo sus eventos propios sin importar el estado.

Los usuarios con el rol "CLIENT" pueden realizar busqueda de eventos con los siguientes filtros:
- title
- start_date
- finish_date
- category_id
El filtro "available" siempre debe estar en true cuando el "CLIENT" realiza petición de listado.

Dentro de este modulo se trabajan 3 tablas, que son:
- event_category
- event_images
- event

Los eventos pueden incluir varias imagenes de tipo jpg, jpeg y png, solo de esos 3 tipo.

Debe haber operaciones CRUD