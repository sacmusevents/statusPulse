# StatusPulse - Angular & Firebase Learning Guide

Welcome! This folder (`angular-app/`) contains a modern, structured Angular workspace created to help you learn Angular hands-on by rebuilding the **StatusPulse** real-time feedback application using **Firebase**.

---

## 🚀 How to Run the App

Open your terminal in the `angular-app/` directory and run:

```bash
npm start
```

Then open your browser to [http://localhost:4200](http://localhost:4200).

---

## 📁 Project Organization & Architecture

```
src/app/
├── environments/
│   └── environment.ts             # 1️⃣ Firebase credentials configuration
├── models/
│   └── session.model.ts           # 2️⃣ Data interfaces (Session, SignalState)
├── services/
│   └── firebase.service.ts        # 3️⃣ Firebase Service (@Injectable & Dependency Injection)
├── pipes/
│   └── relative-time.pipe.ts      # 4️⃣ Custom Pipe (@Pipe & PipeTransform for relative dates)
├── components/
│   ├── session-list/              # 5️⃣ Session List Screen (Signals, @if, @for control flow)
│   └── light-display/             # 6️⃣ Light Display Screen (ActivatedRoute, lifecycle hooks)
├── app.routes.ts                  # 7️⃣ Angular Router configuration
└── app.html                       # 8️⃣ App Container (<router-outlet />)
```

---

## 🎯 Learning Steps (Your Hands-On Roadmap)

### Step 1: Set up Firebase Credentials
Open `src/app/environments/environment.ts` and paste your project config keys from your [Firebase Console](https://console.firebase.google.com/).

### Step 2: Implement Real Firebase Data Fetching
Open `src/app/services/firebase.service.ts` and inspect:
- `getActiveSessions()` -> Replace mock array with Firebase Realtime Database query (`get(ref(db, 'sessions'))`).
- `subscribeToSignal(sessionId, callback)` -> Wire up real-time listener using Firebase `onValue(ref(db, ...))`.

### Step 3: Learn Angular Pipes
Open `src/app/pipes/relative-time.pipe.ts` to see how values in HTML like `{{ session.lastUpdated | relativeTime }}` get formatted dynamically.

### Step 4: Master Angular Signals & Control Flow
Open `src/app/components/session-list/session-list.component.ts` and `session-list.component.html`:
- Notice how `signal<Session[]>()` holds component state reactively.
- Notice `@if (isLoading())` and `@for (session of sessions(); track session.id)` syntax in HTML templates.

### Step 5: Master Routing & Lifecycle Hooks
Open `src/app/components/light-display/light-display.component.ts`:
- Notice `inject(ActivatedRoute)` to extract parameter `:id` from route `/light/:id`.
- Notice `ngOnInit()` for subscribing to data and `ngOnDestroy()` for cleaning up database listeners when leaving the page.
