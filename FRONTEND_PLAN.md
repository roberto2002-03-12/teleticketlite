El usuario al realizar el login le debe aparecer una diferente interfaz dependiendo del rol que este tenga, solo se va considerar para 3 roles, los cuales son:
- CLIENT
- OWNER
- STAFF

La API Rest que va utilizar para realizar interacciones es: `https://ds5cqe1hk8.execute-api.us-east-1.amazonaws.com`

Los endpoints que se van a utilizar van a ser los siguientes:

GENERAL SIN AUTH
- /auth/login
```
{
    "code": 200,
    "data": {
        "accessToken": "XXXXXXXXXXXXXXXXXXXXXXXXXX",
        "idToken": "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
        "refreshToken": "XXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
        "expiresIn": 3600,
        "tokenType": "Bearer"
    },
    "error": null,
    "status": "success"
}
```
---

- /users POST
```
{
    "code": 201,
    "data": {
        "id": 9,
        "email": "robertocg25dev@gmail.com",
        "phoneNumber": "+51915368205",
        "fullname": "Roberto Contreras Gonzales OWNER",
        "birthdate": "2002-03-12",
        "dni": "01133423",
        "photoUrl": null,
        "role": "CLIENT"
    },
    "error": null,
    "status": "success"
}
```
---

GENERAL CON AUTH
- /events/categories GET (no te pide token pero a nivel de interfaz solo debería mostrarlo si el usuario esta autenticado)
```
{
    "code": 200,
    "data": [
        {
            "id": 1,
            "name": "FIESTAS",
            "description": "fiestas p, q más va ser?"
        }
    ],
    "error": null,
    "status": "success"
}
```

- /users/me GET
```
{
    "code": 200,
    "data": {
        "id": 11,
        "email": "staff3@example.com",
        "phoneNumber": "+51907611121",
        "fullname": "Staff Member",
        "birthdate": "1990-01-01",
        "dni": "87601322",
        "photoUrl": null,
        "role": "STAFF"
    },
    "error": null,
    "status": "success"
}
```

- /users/me PUT
```
{
    "code": 200,
    "data": {
        "id": 8,
        "email": "robertocg24dev@gmail.com",
        "phoneNumber": "+51987659999",
        "fullname": "Roberto Contreras Gonzales",
        "birthdate": "2002-03-12",
        "dni": "12345676",
        "photoUrl": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/profile-pictures/8/ee936296-f148-4dfa-9c37-d0eb9c474aa6.jpg",
        "role": "CLIENT"
    },
    "error": null,
    "status": "success"
}
```

- /users/me/photo POST
```
{
    "code": 200,
    "data": {
        "id": 8,
        "email": "robertocg24dev@gmail.com",
        "phoneNumber": "+51925368203",
        "fullname": "Roberto Contreras Gonzales OWNER",
        "birthdate": "2002-03-12",
        "dni": "01133323",
        "photoUrl": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/profile-pictures/8/7416aa11-e035-4276-b9d8-37c6db8565d3.jpg",
        "role": "CLIENT"
    },
    "error": null,
    "status": "success"
}
```

- /users/me/photo DELETE
```
{
    "code": 200,
    "data": null,
    "error": null,
    "status": "success"
}
```
---

OWNER
- /events POST
```
{
    "code": 201,
    "data": {
        "id": 4,
        "title": "Radahn Vs Malenia",
        "description": "Es de un juego",
        "maxPeople": 2,
        "address": "Mundo virtual",
        "available": true,
        "finished": false,
        "startDate": "2026-08-01T20:00:00",
        "finishDate": "2026-08-02T02:00:00",
        "ownerId": 5,
        "categoryId": 1,
        "ownerFullName": null,
        "categoryName": null,
        "images": [
            {
                "id": 16,
                "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/0-964ed32a-4af0-4ce4-bc23-838dd3fb52d9.jpg",
                "index": 0
            },
            {
                "id": 17,
                "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/1-7487fbd9-3dd3-4c7d-8439-09961eb48aba.jpg",
                "index": 1
            },
            {
                "id": 18,
                "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/2-5cb8c44a-d42c-41f0-8bf1-45ad220695d5.jpg",
                "index": 2
            }
        ]
    },
    "error": null,
    "status": "success"
}
```

- /events/{eventId} PUT
```
{
    "code": 200,
    "data": {
        "id": 4,
        "title": "Radahn Vs Malenia",
        "description": "Es de un juego",
        "maxPeople": 2,
        "address": "Mundo virtual",
        "available": true,
        "finished": false,
        "startDate": "2026-08-01T20:00:00",
        "finishDate": "2026-08-02T02:00:00",
        "ownerId": 5,
        "categoryId": 2,
        "ownerFullName": null,
        "categoryName": null,
        "images": [
            {
                "id": 16,
                "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/0-964ed32a-4af0-4ce4-bc23-838dd3fb52d9.jpg",
                "index": 0
            },
            {
                "id": 17,
                "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/1-7487fbd9-3dd3-4c7d-8439-09961eb48aba.jpg",
                "index": 1
            },
            {
                "id": 18,
                "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/2-5cb8c44a-d42c-41f0-8bf1-45ad220695d5.jpg",
                "index": 2
            }
        ]
    },
    "error": null,
    "status": "success"
}
```

- /events/{eventId}/images PUT
```
{
    "code": 200,
    "data": {
        "id": 4,
        "title": "Radahn Vs Malenia",
        "description": "Es de un juego",
        "maxPeople": 2,
        "address": "Mundo virtual",
        "available": true,
        "finished": false,
        "startDate": "2026-08-01T20:00:00",
        "finishDate": "2026-08-02T02:00:00",
        "ownerId": 5,
        "categoryId": 2,
        "ownerFullName": null,
        "categoryName": null,
        "images": [
            {
                "id": 19,
                "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/0-3e0cf097-b43f-4383-969e-2bf62b7c6d94.jpg",
                "index": 0
            },
            {
                "id": 20,
                "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/1-fe384a23-8a54-419c-9f9d-be5111e7a3b3.jpg",
                "index": 1
            },
            {
                "id": 21,
                "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/2-64c499aa-fbde-406f-9f0d-dffc64647f83.jpg",
                "index": 2
            },
            {
                "id": 22,
                "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/3-8d727fc9-c88b-4e14-aabe-b6c0e78901b3.jpg",
                "index": 3
            }
        ]
    },
    "error": null,
    "status": "success"
}
```

- /events/{eventId}/cancel PUT
```
{
    "code": 200,
    "data": {
        "id": 4,
        "title": "Radahn Vs Malenia",
        "description": "Es de un juego",
        "maxPeople": 2,
        "address": "Mundo virtual",
        "available": false,
        "finished": false,
        "startDate": "2026-08-01T20:00:00",
        "finishDate": "2026-08-02T02:00:00",
        "ownerId": 5,
        "categoryId": 2,
        "ownerFullName": null,
        "categoryName": null,
        "images": [
            {
                "id": 19,
                "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/0-3e0cf097-b43f-4383-969e-2bf62b7c6d94.jpg",
                "index": 0
            },
            {
                "id": 20,
                "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/1-fe384a23-8a54-419c-9f9d-be5111e7a3b3.jpg",
                "index": 1
            },
            {
                "id": 21,
                "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/2-64c499aa-fbde-406f-9f0d-dffc64647f83.jpg",
                "index": 2
            },
            {
                "id": 22,
                "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/3-8d727fc9-c88b-4e14-aabe-b6c0e78901b3.jpg",
                "index": 3
            }
        ]
    },
    "error": null,
    "status": "success"
}
```

- /events/me GET
```
{
    "code": 200,
    "data": [
        {
            "id": 4,
            "title": "Radahn Vs Malenia",
            "description": "Es de un juego",
            "maxPeople": 2,
            "address": "Mundo virtual",
            "available": false,
            "finished": false,
            "startDate": "2026-08-01T20:00:00",
            "finishDate": "2026-08-02T02:00:00",
            "ownerId": 5,
            "categoryId": 2,
            "ownerFullName": null,
            "categoryName": null,
            "images": [
                {
                    "id": 19,
                    "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/0-3e0cf097-b43f-4383-969e-2bf62b7c6d94.jpg",
                    "index": 0
                },
                {
                    "id": 20,
                    "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/1-fe384a23-8a54-419c-9f9d-be5111e7a3b3.jpg",
                    "index": 1
                },
                {
                    "id": 21,
                    "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/2-64c499aa-fbde-406f-9f0d-dffc64647f83.jpg",
                    "index": 2
                },
                {
                    "id": 22,
                    "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/3-8d727fc9-c88b-4e14-aabe-b6c0e78901b3.jpg",
                    "index": 3
                }
            ]
        }
    ],
    "error": null,
    "status": "success"
}
```

- /events/{eventId}/images DELETE
```
{
    "code": 200,
    "data": {
        "id": 3,
        "title": "Rock Concert",
        "description": "Live rock music event downtown.",
        "maxPeople": 200,
        "address": "Av. Javier Prado 1234, Lima",
        "available": true,
        "finished": false,
        "startDate": "2026-08-01T20:00:00",
        "finishDate": "2026-08-02T02:00:00",
        "ownerId": 4,
        "categoryId": 1,
        "ownerFullName": null,
        "categoryName": null,
        "images": [
            {
                "id": 11,
                "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/3/1-73704d56-6c72-4f37-9ddc-6585c29219a4.jpg",
                "index": 1
            },
            {
                "id": 14,
                "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/3/4-e1c73c6d-0428-4713-a3fc-bddf8f43acf0.jpg",
                "index": 4
            },
            {
                "id": 15,
                "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/3/5-44da7c5a-205e-4121-b533-ebce27c3c685.jpg",
                "index": 5
            }
        ]
    },
    "error": null,
    "status": "success"
}
```

- /qr/tickets/validate POST
```
{
    "code": 200,
    "data": {
        "valid": false
    },
    "error": null,
    "status": "success"
}
```

- /users/staff POST
```
{
    "code": 201,
    "data": {
        "id": 11,
        "email": "staff3@example.com",
        "phoneNumber": "+51907611121",
        "fullname": "Staff Member",
        "birthdate": "1990-01-01",
        "dni": "87601322",
        "photoUrl": null,
        "role": "STAFF"
    },
    "error": null,
    "status": "success"
}
```

- /staff/affiliate POST
```
{
    "code": 200,
    "data": null,
    "error": null,
    "status": "success"
}
```

- /users/staff/disaffiliate DELETE
```
{
    "code": 200,
    "data": null,
    "error": null,
    "status": "success"
}
```

- /qr/events/{eventId}/assistants
```
{
    "code": 200,
    "data": [
        {
            "id": 1,
            "userId": 8,
            "eventId": 3,
            "registerDate": "2026-07-13T23:44:47"
        }
    ],
    "error": null,
    "status": "success"
}
```
---

STAFF
- /events/me/staff GET
```
{
    "code": 200,
    "data": [
        {
            "id": 4,
            "title": "Radahn Vs Malenia",
            "description": "Es de un juego",
            "maxPeople": 2,
            "address": "Mundo virtual",
            "available": false,
            "finished": false,
            "startDate": "2026-08-01T20:00:00",
            "finishDate": "2026-08-02T02:00:00",
            "ownerId": 5,
            "categoryId": 2,
            "ownerFullName": null,
            "categoryName": null,
            "images": [
                {
                    "id": 19,
                    "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/0-3e0cf097-b43f-4383-969e-2bf62b7c6d94.jpg",
                    "index": 0
                },
                {
                    "id": 20,
                    "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/1-fe384a23-8a54-419c-9f9d-be5111e7a3b3.jpg",
                    "index": 1
                },
                {
                    "id": 21,
                    "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/2-64c499aa-fbde-406f-9f0d-dffc64647f83.jpg",
                    "index": 2
                },
                {
                    "id": 22,
                    "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/3-8d727fc9-c88b-4e14-aabe-b6c0e78901b3.jpg",
                    "index": 3
                }
            ]
        }
    ],
    "error": null,
    "status": "success"
}
```

- /events/{eventId}/staff PUT
```
{
    "code": 200,
    "data": {
        "id": 4,
        "title": "Radahn Vs Malenia",
        "description": "Fight to death",
        "maxPeople": 2,
        "address": "Mundo virtual",
        "available": false,
        "finished": false,
        "startDate": "2026-08-01T20:00:00",
        "finishDate": "2026-08-02T02:00:00",
        "ownerId": 5,
        "categoryId": 2,
        "ownerFullName": null,
        "categoryName": null,
        "images": [
            {
                "id": 19,
                "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/0-3e0cf097-b43f-4383-969e-2bf62b7c6d94.jpg",
                "index": 0
            },
            {
                "id": 20,
                "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/1-fe384a23-8a54-419c-9f9d-be5111e7a3b3.jpg",
                "index": 1
            },
            {
                "id": 21,
                "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/2-64c499aa-fbde-406f-9f0d-dffc64647f83.jpg",
                "index": 2
            },
            {
                "id": 22,
                "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/4/3-8d727fc9-c88b-4e14-aabe-b6c0e78901b3.jpg",
                "index": 3
            }
        ]
    },
    "error": null,
    "status": "success"
}
```

- /qr/tickets/validate POST
```
{
    "code": 200,
    "data": {
        "valid": false
    },
    "error": null,
    "status": "success"
}
```
---

CLIENT
- /qr/events/{eventId}/tickets POST
```
{
    "code": 201,
    "data": {
        "id": 1,
        "userId": 8,
        "eventId": 3,
        "qrUrl": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/qr-codes/8/3-e27c09b0-1537-4005-81b5-bc2edf318dc3.png",
        "alreadyApplied": false
    },
    "error": null,
    "status": "success"
}
```

- /qr/me/events GET
```
{
    "code": 200,
    "data": [
        {
            "qrUrl": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/qr-codes/8/3-e27c09b0-1537-4005-81b5-bc2edf318dc3.png",
            "alreadyApplied": true,
            "eventName": "Rock Concert",
            "eventAddress": "Av. Javier Prado 1234, Lima",
            "startDate": "2026-08-01T20:00:00",
            "endDate": "2026-08-02T02:00:00"
        }
    ],
    "error": null,
    "status": "success"
}
```

- /events?title={queryTitle}&startDate={queryStartDate}&finishDate={queryFinishDate}&categoryId={queryCategoryId}&page={queryNroPage} (ejemplo: /events?title=Concert&startDate=2026-01-01T00:00:00&finishDate=2026-12-31T23:59:59&categoryId=1&page=0)
```
{
    "code": 200,
    "data": {
        "items": [
            {
                "id": 1,
                "title": "title",
                "description": "description",
                "maxPeople": 200,
                "address": "dadada",
                "available": true,
                "finished": false,
                "startDate": "2026-07-09T00:00:00",
                "finishDate": "2026-07-09T00:00:00",
                "ownerId": 1,
                "categoryId": 1,
                "ownerFullName": "Roberto Contreras Gonzales OWNER",
                "categoryName": "FIESTAS",
                "images": []
            },
            {
                "id": 3,
                "title": "Rock Concert",
                "description": "Live rock music event downtown.",
                "maxPeople": 200,
                "address": "Av. Javier Prado 1234, Lima",
                "available": true,
                "finished": false,
                "startDate": "2026-08-01T20:00:00",
                "finishDate": "2026-08-02T02:00:00",
                "ownerId": 4,
                "categoryId": 1,
                "ownerFullName": "Event Owner",
                "categoryName": "FIESTAS",
                "images": [
                    {
                        "id": 10,
                        "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/3/0-f93259ab-c771-4417-86e3-b54ad13a4eee.jpg",
                        "index": 0
                    },
                    {
                        "id": 11,
                        "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/3/1-73704d56-6c72-4f37-9ddc-6585c29219a4.jpg",
                        "index": 1
                    },
                    {
                        "id": 14,
                        "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/3/4-e1c73c6d-0428-4713-a3fc-bddf8f43acf0.jpg",
                        "index": 4
                    },
                    {
                        "id": 15,
                        "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/3/5-44da7c5a-205e-4121-b533-ebce27c3c685.jpg",
                        "index": 5
                    }
                ]
            }
        ],
        "total": 2,
        "page": 0,
        "pageSize": 12,
        "totalPages": 1
    },
    "error": null,
    "status": "success"
}
```

- /events/{eventId}
```
{
    "code": 200,
    "data": {
        "id": 3,
        "title": "Rock Concert",
        "description": "Live rock music event downtown.",
        "maxPeople": 200,
        "address": "Av. Javier Prado 1234, Lima",
        "available": true,
        "finished": false,
        "startDate": "2026-08-01T20:00:00",
        "finishDate": "2026-08-02T02:00:00",
        "ownerId": 4,
        "categoryId": 1,
        "ownerFullName": "Event Owner",
        "categoryName": "FIESTAS",
        "images": [
            {
                "id": 10,
                "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/3/0-f93259ab-c771-4417-86e3-b54ad13a4eee.jpg",
                "index": 0
            },
            {
                "id": 11,
                "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/3/1-73704d56-6c72-4f37-9ddc-6585c29219a4.jpg",
                "index": 1
            },
            {
                "id": 14,
                "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/3/4-e1c73c6d-0428-4713-a3fc-bddf8f43acf0.jpg",
                "index": 4
            },
            {
                "id": 15,
                "url": "https://teleticket-lite-images-561547870320.s3.us-east-1.amazonaws.com/event-images/3/5-44da7c5a-205e-4121-b533-ebce27c3c685.jpg",
                "index": 5
            }
        ]
    },
    "error": null,
    "status": "success"
}
```

Verás que hay algunos endpoints que no se han declarado, esto es porque solo va ser utilizado mediante API para el rol admin.
