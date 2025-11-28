Blocker — Parental Control & Digital Well-Being App

A powerful and privacy-focused Android parental control application built using Kotlin, Jetpack Compose, and AccessibilityService.
Blocker empowers parents to guide and monitor children’s smartphone usage through in-app blocking, screen-time limits, website/keyword filtering, and detailed usage analytics — all wrapped in a clean, modern UI.

📌 Play Store Release: Coming Soon 🚀
📌 Built Using: Kotlin · Jetpack Compose · MVVM · Room · AccessibilityService · WorkManager

🚀 Features
🔒 Digital Controls

Block distracting content like:

Instagram Reels

YouTube Shorts

Snapchat Stories

App-level screen-time limits

Website blocking & keyword-based filtering

Dual Parent Mode & Child Mode

📊 Monitoring & Analytics

Daily and session-based usage tracking

Visual usage graphs & insights

Real-time usage alerts

Session count, logs & activity summaries

⚙️ Reliable Background Enforcement

Built on AccessibilityService for precise in-app blocking

Foreground Service for uninterrupted monitoring

WorkManager for periodic tasks

Battery optimization handling to maintain uptime

Achieves 95%+ stability across physical devices

🎨 Modern UI / UX

Jetpack Compose UI with smooth animations

Clean layout optimized for ease of use

Dark mode support

Simple onboarding for parents & kids

🛠️ Tech Stack

Language:

Kotlin (Coroutines, Flows)

UI Layer:

Jetpack Compose

Material 3

Navigation Compose

Architecture:

MVVM (ViewModel + LiveData)

Repository Pattern

Clean modular structure

System Components:

AccessibilityService

Foreground Services

Broadcast Receivers

WorkManager

Room Database

Tools:

Android Studio Koala

ADB tools for debugging

Firebase Crashlytics (optional if added later)

📸 Screenshots

<img width="140" height="296" alt="image" src="https://github.com/user-attachments/assets/b905d3dc-547b-4c13-9d5d-3eff343c93a9" />
<img width="140" height="296" alt="image" src="https://github.com/user-attachments/assets/6a42578c-f1f9-4c60-886f-46169e754869" />
<img width="140" height="296" alt="image" src="https://github.com/user-attachments/assets/3487580b-ea49-4863-8d62-7510e46485ad" />
<img width="140" height="296" alt="image" src="https://github.com/user-attachments/assets/13773c5b-d845-4ea7-8202-4f153aa3770d" />
<img width="140" height="296" alt="image" src="https://github.com/user-attachments/assets/2b5fb78a-6c31-4e4f-9c2e-074f36bd6625" />
<img width="140" height="296" alt="image" src="https://github.com/user-attachments/assets/3f9e6778-d1dd-4369-a2af-b8562b825da2" />
<img width="140" height="296" alt="image" src="https://github.com/user-attachments/assets/65dc41ff-c0ef-48c8-b5d4-b5d713cfb771" />
<img width="140" height="296" alt="image" src="https://github.com/user-attachments/assets/bee1bebb-e2f4-454b-8d72-dc496349435f" />
<img width="140" height="296" alt="image" src="https://github.com/user-attachments/assets/0cdfe7bb-fffe-4b6b-aaf0-6940ad2d88c9" />
<img width="140" height="296" alt="image" src="https://github.com/user-attachments/assets/147f5c75-e6d6-4a55-a1df-bd48eb4e7cff" />
<img width="140" height="296" alt="image" src="https://github.com/user-attachments/assets/578db787-303a-49a2-b67e-f7f5656a1d00" />
<img width="140" height="296" alt="image" src="https://github.com/user-attachments/assets/725440ce-18d3-401b-afc4-2766fdda1293" />


🧩 Project Structure
Blocker/
 ├── app/
 │   ├── data/
 │   │   ├── database/ (Room entities, DAO)
 │   │   ├── repository/
 │   ├── domain/
 │   │   ├── models/
 │   │   └── usecases/
 │   ├── ui/
 │   │   ├── components/
 │   │   ├── screens/
 │   │   └── theme/
 │   ├── services/
 │   │   ├── AccessibilityService
 │   │   └── ForegroundService
 │   └── utils/
 └── README.md

⚡ Installation & Setup

Clone the repository

git clone https://github.com/Manthan0304/Blocker.git


Open the project in Android Studio Koala (or later)

Sync dependencies

Run the app on a physical Android device

⚠️ AccessibilityService does not fully work on emulators.

Enable the required permissions (Accessibility, Usage Access, Notifications)

🧪 Testing & Stability

Tested across multiple real devices

Handles aggressive background restrictions using:

Battery optimization exceptions

Foreground service prioritization

Crash rate reduced via Room persistence & MVVM separation

🎯 Roadmap

🌐 Cloud sync for logs & parent dashboard

🧠 AI-powered behaviour insights

☁️ Server-based blocking rules & remote monitoring

🧩 Widget support for quick actions

🔒 Enhanced privacy dashboard
