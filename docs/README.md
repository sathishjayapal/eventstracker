# 🛠️ Sathish Jayapal – Laboratory of Systems & Resilience

## 📌 EventsTracker Documentation Freshness Policy

To keep EventsTracker in a true and readable state for everyone:

- Any important code/configuration/operational change must include a README update in the same check-in.
- CI now enforces this through `.github/workflows/readme-guard.yml`.
- For EventsTracker updates, use:
  - `/home/runner/work/eventstracker/eventstracker/README.md`
  - `/home/runner/work/eventstracker/eventstracker/docs/README.md`

If implementation changes are not reflected in README, the check-in should be considered incomplete.

**Cloud Architect | Event-Driven Systems Builder | Marathon Runner | Learning in Public**

I design distributed systems for cloud platforms and explore how **resilience principles** from endurance sports apply to building reliable software. This is my laboratory — where architecture thinking meets code, and theory meets the constraints of running real applications.

---

## 🎯 Where to Start

**If you're interested in:**

### 📐 **Event-Driven Systems & Distributed Transactions**
→ Start with **[EventsTracker](https://github.com/sathishjayapal/eventstracker)**  
A production-grade multi-service platform exploring RabbitMQ choreography, ShedLock coordination, and Kubernetes operations. Built to answer: *How do you handle distributed transactions and race conditions at scale?*

### 🏃 **Analytics for Distributed Systems (via Running Data)**
→ Start with **[Runs App](https://github.com/sathishjayapal/runs-ai-analyzer)** *(WIP)*  
Using semantic caching (PGVector + Claude) to analyze running data as a testbed for RAG patterns and real-time anomaly detection. Why? Because marathons taught me that **resilience is a system property**, not a component.

### 🏗️ **Infrastructure as Code & Kubernetes Ops**
→ Start with **[EKS Terraform Labs](https://github.com/sathishjayapal/eks-infra-labs)** *(WIP)*  
Reverse-engineering cloud-click clusters into versioned, reviewed, reproducible infrastructure. Learning to go from "eksctl create cluster" to "infrastructure as a git-reviewed system."

### 🤖 **Agentic AI for Engineering Workflows**
→ Explore **[AI Agent Experiments](https://github.com/sathishjayapal?q=ai-agent&type=source)**  
Auto-triaging stale branches, reconciling Terraform state with live resources, drafting ADRs from commit history. Early-stage exploration of how AI agents can reduce toil.

---

## 📚 Architecture Deep Dives (Read First, Then Code)

I write longer pieces at **[sathishjayapal.me](https://sathishjayapal.me)** (canonical source) and cross-post to [Medium @dotsky](https://dotsky.medium.com).

### Featured Posts (Start Here)
- **[From eksctl to Terraform: Making Sense of EKS Resources](https://sathishjayapal.me/eksctl-to-terraform-eks-mapping/)**  
  How to take an EKS cluster created with `eksctl` and reverse-engineer it into maintainable Terraform modules. The gap between "click-next cloud" and "infrastructure you can version and review."

- **[Designing Scalable Queues for Real-World Workloads](https://sathishjayapal.me/2025/10/24/designing-scalable-queues-for-personal-projects/)**  
  Patterns for moving RabbitMQ from hobby projects to resilient production-like setups: dead-lettering, backpressure, observability. This thinking is baked into EventsTracker.

- **[Tackling Distributed Transactions in Microservices](https://dotsky.medium.com/tackling-distributed-transactions)** *(cross-posted)*  
  Using ShedLock for distributed task scheduling and avoiding race conditions in Kubernetes. Real constraints. Real solutions.

- **[From Marathon Dreams to Injury Recovery: A Runner's Journey](https://sathishjayapal.me/2025/12/31/from-marathon-dreams-to-injury-recovery-a-runners-journey/)**  
  How systems thinking from distributed systems applies to running recovery, feedback loops, and building resilience into training design.

- **[Semantic Caching for Intelligent Running Analysis](https://sathishjayapal.me/2026/02/08/semantic-caching-for-intelligent-running-analysis/)**  
  Using PGVector and Claude embeddings to avoid re-analyzing past running data. RAG patterns at personal scale.

**→ [See all posts](https://sathishjayapal.me)**

---

## 🏗️ What I'm Building Now

### EventsTracker — **MVP + Active**
A multi-service event ingestion platform built as a learning vehicle for Kubernetes, Spring Cloud, and distributed systems.
- **Why:** To understand how production systems handle distributed transactions, race conditions, and resilience at small scale before enterprise scale.
- **Tech:** Java 21 • Spring Boot 4.0 • RabbitMQ • PostgreSQL/Flyway • Kubernetes • Maven
- **Focus:** Event-driven choreography, ShedLock coordination, zero-trust microservice security.
- **Status:** Core event ingestion stable; Kubernetes deployment in progress.
- **Next:** Zero-downtime deployments, full observability (metrics/tracing).

→ **[Go to EventsTracker](https://github.com/sathishjayapal/eventstracker)** | **[Read the blog post](https://sathishjayapal.me/tackling-distributed-transactions)**

---

### Runs App — **MVP + Active Learning**
A multi-service platform for ingesting and analyzing running data from Garmin/Strava.
- **Why:** Marathons taught me that resilience is a system property. I'm applying that insight to real-time athletic performance analytics.
- **Tech:** Java • Spring Boot • PGVector • PostgreSQL • Claude API • RabbitMQ/Kafka • Kubernetes (WIP)
- **Focus:** RAG-based semantic caching, real-time injury pattern detection, agentic AI coaching recommendations.
- **Status:** Garmin ingestion stable; semantic analysis in progress.
- **Next:** Kubernetes deployment, multi-region eventual consistency patterns.

→ **[Go to Runs App](https://github.com/sathishjayapal/runs-ai-analyzer)** | **[Read the blog post](https://sathishjayapal.me/semantic-caching-running-analysis)**

---

### EKS Terraform Labs — **Learning Phase**
Reverse-engineering EKS clusters created with `eksctl` into clean, versioned Terraform modules.
- **Why:** Too many teams run "cloud click-next" deployments. This is how you move from ad-hoc to reviewable infrastructure.
- **Tech:** Terraform • AWS EKS • Kubernetes • Infrastructure as Code
- **Status:** Early exploration; learning the mapping from eksctl-generated resources to idiomatic Terraform.

→ **[Go to EKS Labs](https://github.com/sathishjayapal/eks-infra-labs)** | **[Read the blog post](https://sathishjayapal.me/eksctl-to-terraform-eks-mapping/)**

---

### Agentic AI Experiments — **Early Stage**
Exploring AI agents to reduce engineering toil:
- Auto-triaging stale branches and PRs
- Reconciling Terraform state with live Kubernetes/EKS/AKS resources
- Drafting ADRs and changelogs from commit history

→ **[Browse AI experiments](https://github.com/sathishjayapal?q=ai-agent&type=source)**

---

## 💻 Technical Comfort Zone

**Languages & Frameworks**  
Java • Spring Boot • Spring Cloud • REST APIs • GraphQL • Event-Driven Architectures

**Cloud & Infrastructure**  
AWS (EKS, RDS, S3) • Azure • Kubernetes • Terraform • Infrastructure as Code

**Data & Patterns**  
PostgreSQL • RabbitMQ/Kafka • Distributed Transactions • PGVector/Semantic Search • Real-Time Analytics

**Architecture Styles**  
Microservices • Event-Driven • Domain-Driven Design • CQRS • Zero-Trust Security

![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/SpringBoot-6DB33F?style=flat-square&logo=spring-boot&logoColor=white)
![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?style=flat-square&logo=kubernetes&logoColor=white)
![Terraform](https://img.shields.io/badge/Terraform-7B42BC?style=flat-square&logo=terraform&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-232F3E?style=flat-square&logo=amazon-aws&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-336791?style=flat-square&logo=postgresql&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=flat-square&logo=rabbitmq&logoColor=white)

---

## 🏃‍♂️ Beyond Code

**Marathoner:** 9 marathon finishes. Every long run is a lesson in system design — feedback loops, resilience, constraint management, recovery.

**Thesis:** The principles that make distributed systems resilient (redundancy, graceful degradation, observability, feedback loops) are the same principles that make training cycles effective. I explore this at the intersection of both domains.

**Location:** Madison/Sun Prairie, Wisconsin. Always happy to discuss architecture over South Indian coffee.

---

## 🌐 Stay Connected

📝 **Blog** — [sathishjayapal.me](https://sathishjayapal.me) (canonical source of all posts)  
🔗 **Medium** — [@dotsky](https://dotsky.medium.com) (cross-posted, always with canonical link back)  
💼 **LinkedIn** — [linkedin.com/in/sathishjayapal](https://linkedin.com/in/sathishjayapal) *(if using for professional visibility)*  
🐦 **Twitter/Bluesky** — [handle] *(optional)*  

**Interested in collaborating, discussing architecture, or connecting on cloud modernization?**  
→ Open an issue on any repo or reach out at **[email or contact method]**

---

## 📊 Recent Activity

- **Deployed:** EventsTracker event ingestion MVP on Kubernetes (WIP zero-downtime deployments)
- **Learning:** CKAD certification + Kubernetes-native application design
- **Writing:** "Semantic Caching for Intelligent Running Analysis" + deep-dive on agentic AI for engineering workflows
- **Running:** Training cycle 2026, targeting Flying Pig Marathon (May 3, 2026); applying injury-prevention systems thinking

---

## 📝 How to Use This Space

✅ **Learn from the code:** Each project has a detailed README explaining the "why" alongside the "how."  
✅ **Read the architecture posts first:** Blog posts provide context for why code is structured the way it is.  
✅ **Follow the learning journey:** From CKAD exploration → EventsTracker → Kubernetes ops patterns.  
✅ **Engage & discuss:** Open issues for questions, architecture debates, or alternative approaches.  
✅ **Contribute:** Forks, PRs, and improvements welcome.

---

## 🎓 What This Lab is About

This is **not a portfolio of finished products**. It's a **learning laboratory in public**:
- Real constraints (Kubernetes, distributed transactions, RAG patterns)
- Real decisions (documented in Architecture Decision Records)
- Real friction (MapStruct compilation, reconciling Terraform state)
- Real outcomes (blog posts, working applications, operational insights)

The goal is to **show how I think**, not just what I've built.

---

**Built with ☕ and 🏃. Always learning. Always building. Always honest.**
