# 📚 BukiMind
> A modern full-stack digital library platform with AI-powered features

<div align="center">

![BukiMind](https://via.placeholder.com/800x400/4f46e5/ffffff?text=BukiMind)

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://reactjs.org/)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.109-009688?style=for-the-badge&logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com/)
[![MySQL](https://img.shields.io/badge/MySQL-Aiven_Cloud-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://mysql.com/)

**[🌐 Live Demo](https://bukimind-frontend-ecru.vercel.app/)** •
**[📖 API Docs](https://bukimind-backend-d481.onrender.com/swagger-ui/index.html)** •
**[🎥 Demo Video](https://www.youtube.com/watch?v=uis-1R07yUM)**

</div>

---

## 🌟 Highlights
| 🚀 **Modern Architecture**                     | 🔒 **Security**                                             | 💬 **AI & Smart Features**                          | 🎯 **Performance & UX**                                |
|:-----------------------------------------------| :---------------------------------------------------------- |:----------------------------------------------------| :----------------------------------------------------- |
| **Spring Boot 3 + React 18** full-stack design | **JWT Authentication** + Role-based access (Admin/Customer) | **Gemini AI Integration** for smart chatbot         | **Optimized SQL queries** and **efficient pagination** |
| **FastAPI Microservice** for ML/AI tasks       | Secured REST endpoints                                      | AI-powered book discovery & chatbot                 | Clean, modern UI with TailwindCSS                      |
| 3 tier Layered RESTful API architecture        | Input validation & exception handling                       | Context-aware responses                             | Smooth navigation via React Router                     |
| MySQL (Aiven Cloud) + Cloudinary               | Token refresh & secure sessions                             | **Toxic comment detection** with ML                 | Real-time feedback experience                          |
| Render + Netlify cloud deployment              | Rate limiting & API security                                | Natural language query support                      |              |

---

## ✨ Key Features

<div align="center">

|               📚 BukiMind                    |     👤 User System    |         💬 Blog & Social         |     🤖 AI Assistant    |      ⭐ Review System       |
|:----------------------------------------------:| :-------------------: |:--------------------------------:| :--------------------: |:--------------------------:|
|           CRUD + search + pagination           | JWT Auth + Role-based |   Blog posts & nested comments   | Chatbot & Smart search |   **Review & feedback**    |
|                Favorites system                |   Profile management  |      Like / reply features       | Gemini API integration | **Toxic review detection** |
|     **Average ratings**                        |  User role management | **Toxic comment/blog detection** | Natural language query |       |

</div>

---

## 🏗️ System Architecture

![System Architecture](https://res.cloudinary.com/duipncbaq/image/upload/v1759678920/diagram-export-10-5-2025-10_38_25-PM_hanweu.jpg)

---

## 🛠️ Technology Stack

- **Backend:** Java 17 · Spring Boot 3 · Spring Security (JWT) · JPA/Hibernate · MySQL 8.0 · Swagger/OpenAPI
- **Frontend:** React 18 · Vite · TailwindCSS · Axios · React Router DOM v6
- **ML/AI Service:** FastAPI · Python 3.10 · PyTorch · HuggingFace (unitary/toxic-bert) · Google Gemini API
- **Cloud & DevOps:** Render · Netlify · Aiven MySQL · Cloudinary · GitHub Actions

---

## 👥 Role-Based Access Control

<div align="center">

### 🔐 Permission Matrix

<details>
<summary>📜 <strong>View Full Role Permission Table</strong></summary>

| Feature / Action                 | 👑 Admin | 👤 Customer |
|----------------------------------| :------: | :---------: |
| **📚 BukiMind**           |          |             |
| View/Search/Filter/Sort books    |     ✅    |      ✅      |
| View book details                |     ✅    |      ✅      |
| Add/Edit/Delete books            |     ✅    |      ❌      |
| **⭐ Review & Rating System**    |          |             |
| View all reviews                 |     ✅    |      ✅      |
| Write/Edit reviews               |     ✅    |      ✅      |
| Delete own reviews               |     ✅    |      ✅      |
| Delete any review                |     ✅    |      ❌      |
| Rate books (1-5 stars)           |     ✅    |      ✅      |
| View average ratings             |     ✅    |      ✅      |
| **👤 User Management**           |          |             |
| Register / Login / Logout        |     ✅    |      ✅      |
| View user list                   |     ✅    |      ❌      |
| Manage users (users info, roles) |     ✅    |      ❌      |
| View/Update own profile          |     ✅    |      ✅      |
| **❤️ Favorites**                 |          |             |
| Add/Remove favorites             |     ✅    |      ✅      |
| View own favorites               |     ✅    |      ✅      |
| **📝 Blog System**               |          |             |
| View all posts                   |     ✅    |      ✅      |
| Create/Edit/Delete own posts     |     ✅    |      ✅      |
| Manage all posts/comments        |     ✅    |      ❌      |
| Like/Unlike posts                |     ✅    |      ✅      |
| Comment / Reply (nested)         |     ✅    |      ✅      |
| **🤖 AI Features**               |          |             |
| Use chatbot                      |     ✅    |      ✅      |
| AI-powered book search           |     ✅    |      ✅      |
| Get content warnings             |     ✅    |      ✅      |
| **⚙️ System & Tools**            |          |             |
| Access API Docs (Swagger)        |     ✅    |      ✅      |
| Handle validation & exceptions   |     ✅    |      ✅      |

</details>

</div>

---

## 🚀 Quick Start Guide

### 🔧 Backend Setup (Spring Boot)
```bash
# Clone the repository
git clone https://github.com/SmaugTHEDrag/bukimind-backend.git
cd bukimind-backend

# Install dependencies
mvn clean install

# Copy the environment template and fill in your own secrets
cp .env.example .env

# Start the infrastructure (PostgreSQL + Redis + Keycloak)
docker compose up -d

# Configure application.properties
# Database Configuration (PostgreSQL)
spring.datasource.url=jdbc:postgresql://localhost:5433/bukimind or your_spring_datasource_url
spring.datasource.username=bukimind or your_spring_datasource_username
spring.datasource.password=your_password

# Keycloak Resource Server (realm: bukimind, client: bukimind-frontend)
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8180/realms/bukimind

# Gemini API Configuration 
gemini.api.key=your_gemini_api_key
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent 

# Cloudinary Configuration 
cloudinary.cloud_name=your_cloud_name
cloudinary.api_key=your_cloudinary_api_key
cloudinary.api_secret=your_cloudinary_api_secret

# FastAPI Service Configuration
fastapi.service.url=http://localhost:8000 or your_fastapi_service_url

# Run the application
mvn spring-boot:run

# 🎉 Backend running at http://localhost:8080
# 📖 Swagger UI: http://localhost:8080/swagger-ui/index.html
```

### 🐍 FastAPI Setup (ML/AI Service)
```bash
# Clone the FastAPI service repository
git clone https://github.com/SmaugTHEDrag/simple_ai_moderation.git
cd simple_ai_moderation

# Install dependencies
pip install -r requirements.txt

# Configure .env file
HF_TOKEN="hf_*****"

# Run the FastAPI service
uvicorn main:app --reload --port 8000

# 🎉 FastAPI running at http://localhost:8000
# 📖 API Docs: http://localhost:8000/docs
```

### 🎨 Frontend Setup
```bash
# Clone the frontend repository
git clone https://github.com/SmaugTHEDrag/bukimind-frontend.git
cd bukimind-frontend

# Install dependencies
npm install

# Start development server
npm run dev

# 🎉 Frontend running at http://localhost:5173
```

---

## 🎥 Demo & Live Links

<div align="center">

### 🌐 **Live Application**
[![Frontend](https://img.shields.io/badge/Frontend-Vercel-00C7B7?style=for-the-badge&logo=vercel)](https://bukimind-frontend-ecru.vercel.app/)
[![Backend](https://img.shields.io/badge/Backend-Render-46E3B7?style=for-the-badge&logo=render)](https://bukimind-backend-d481.onrender.com/swagger-ui/index.html)
[![FastAPI](https://img.shields.io/badge/ML%20Service-Render-96EA2D?style=for-the-badge&logo=render)](https://simple-ai-moderation.onrender.com/docs)
[![API Docs](https://img.shields.io/badge/API%20Docs-Swagger-85EA2D?style=for-the-badge&logo=swagger)](https://bukimind-backend-d481.onrender.com/swagger-ui/index.html)

### 🎬 **Project Walkthrough**
[![Watch the demo](https://img.youtube.com/vi/uis-1R07yUM/0.jpg)](https://www.youtube.com/watch?v=uis-1R07yUM)  
[![Watch on YouTube](https://img.shields.io/badge/▶️%20Watch%20on%20YouTube-FF0000?style=for-the-badge&logo=youtube&logoColor=white)](https://www.youtube.com/watch?v=uis-1R07yUM)

</div>

---
## 👨‍💻 About the Developer
<div align="center">

<h1>Pham Thai Nguyen</h1>

[![GitHub](https://img.shields.io/badge/GitHub-SmaugTHEDrag-181717?style=for-the-badge&logo=github)](https://github.com/SmaugTHEDrag)
[![Email](https://img.shields.io/badge/Email-thainguyen122004@gmail.com-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:thainguyen122004@gmail.com)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-0077B5?style=for-the-badge&logo=linkedin)](https://linkedin.com/in/yourprofile)

*Java Backend developer passionate about creating modern, scalable applications*

</div>

---

<div align="center">

**⭐ If you found this project helpful, please give it a star! ⭐**

Made with ❤️ and ☕ by [Pham Thai Nguyen](https://github.com/SmaugTHEDrag)

---

</div>