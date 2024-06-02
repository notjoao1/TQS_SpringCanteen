# TQS_SpringCanteen
TQS Project UA 2023/2024 - Spring Canteen

## Project Abstract
**Spring Canteen** is a chain of restaurants designed to revolutionize the perception of fast food, by delivering high quality healthy meals in record speed. Customers in a hurry can quickly get a chicken salad with low carbs and not feel guilty after a great meal.

Spring Canteen is an integrated IT solution that provides a restaurant with the ability to efficiently handle different kinds of tasks regarding the management of orders between the staff members and the clients. Spring Canteen will meet these requirements through the implementation of various platforms:
- Spring-Kiosk - A customer portal in which the clients can submit their orders. This platform should provide the customer with menu options and handle the payment processing. It should also give the customer the ability to choose between pickups methods for the order, namely selecting between collecting the order at the counter. At the end of the menu selection process, it should be presented to the user the option to upgrade the order priority status, for an additional fee. 

- Spring-Desk – A platform responsible for providing the staff members with services to handle administrative tasks including employee authentication and payments at the counter. Additionally, there should be presented to the employees’ different service queues, a normal and a priority queue where priority orders should take precedence regarding meal preparation. The staff members will also be able to manage the different waiting lines according to the order’s readiness and delivery confirmation.  

- Spring-Boards - A digital signage solution that concisely communicates to the client that his/her order is being prepared and promptly inform him/her of when it is ready for pickup at the counter. The different orders will appear by the sequence of the order placement, within the different priority queues.

This project aims to implement an MVP compliant with the prior usage scenarios, while applying software enterprise architecture patterns. The development of the solution will follow collaborative agile practices and a Software Quality Assurance (SQA) strategy. Software engineering practices like continuous testing, continuous integration and continuous delivery will be embraced with the aim of reducing manual intervention and saving precious time, promoting collaboration and trust among the software development team, and streamlining the development lifecycle to rapidly deliver high-quality software releases. 

## Project Team

| Name | Email | NMEC | Role |
| ---- | ----- | ---- | ---- |
| José Mendes | mendes.j@ua.pt | 107188 | QA Engineer |
| João Dourado | joao.dourado1@ua.pt | 108636 | Team Leader |
| Miguel Figueiredo | miguel.belchior@ua.pt | 108287 | Product Owner |
| Vítor Santos | vitor.mtsantos@ua.pt | 107186 | DevOps Master |

## Running 

Ensure you have [Docker]([https://](https://www.docker.com/)) installed, with [Docker Compose]([https://](https://docs.docker.com/compose/)) available.

To run the development environment:

```bash
docker compose up -d
```

To run the production environment:

```bash
docker compose -f docker-compose.prod.yml up --build -d
```

## System Demo

The demo can be viewed [here](https://www.youtube.com/watch?v=ugFbt7i12fo&feature=youtu.be).

We simulated the work of all employees (cook, employee handling confirming orders, and employee handling payments), while users placed orders.

## Documentation

[Swagger]([https://](https://swagger.io/)) documentation based on the OpenAPI specification can be accessed at the following endpoint:

- Development: `http://localhost/api/swagger-ui/index.html`
- Production: `http://deti-tqs-02.ua.pt/api/swagger-ui/index.html`


##  Project Bookmarks


### Project Backlog

https://springcanteen.atlassian.net/jira/software/projects/SCRUM/boards/1/backlog


### Project specification report

https://uapt33090-my.sharepoint.com/:w:/g/personal/vitor_mtsantos_ua_pt/EUG5IsPt8vBOj96jJ5s9pnIBJ4QMXVGwoPZtpx56Rn2-Qg?e=tQFoO1

### Project QA Manual

https://uapt33090-my.sharepoint.com/:w:/g/personal/vitor_mtsantos_ua_pt/EVt86ZoggC9Li2d0tWOtDv8BEoIh2DbAfDN6OF3PTO74zA?e=JjDXb5
