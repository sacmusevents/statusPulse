-- Multi-Session Signal Light System - Supabase Database Setup
-- Run these SQL commands in your Supabase project's SQL Editor
-- https://app.supabase.com/project/[YOUR-PROJECT-ID]/sql

-- 1. Create the sessions table
CREATE TABLE IF NOT EXISTS sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  title TEXT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  status TEXT DEFAULT 'active' CHECK (status IN ('active', 'deleted'))
);

-- 2. Create the signals table
CREATE TABLE IF NOT EXISTS signals (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  session_id UUID REFERENCES sessions(id) ON DELETE CASCADE,
  color TEXT NOT NULL DEFAULT 'green' CHECK (color IN ('red', 'yellow', 'green')),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  UNIQUE(session_id)
);

-- 3. Enable Row Level Security (RLS)
ALTER TABLE sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE signals ENABLE ROW LEVEL SECURITY;

-- 4. Create public access policies (allow all for event use case)
CREATE POLICY "Allow public read sessions" ON sessions
  FOR SELECT USING (status = 'active');

CREATE POLICY "Allow public insert sessions" ON sessions
  FOR INSERT WITH CHECK (true);

CREATE POLICY "Allow public update sessions" ON sessions
  FOR UPDATE USING (true) WITH CHECK (true);

CREATE POLICY "Allow public delete sessions" ON sessions
  FOR DELETE USING (true);

CREATE POLICY "Allow public read signals" ON signals
  FOR SELECT USING (true);

CREATE POLICY "Allow public insert signals" ON signals
  FOR INSERT WITH CHECK (true);

CREATE POLICY "Allow public update signals" ON signals
  FOR UPDATE USING (true) WITH CHECK (true);

-- 5. Create indexes for better performance
CREATE INDEX idx_sessions_status ON sessions(status);
CREATE INDEX idx_signals_session_id ON signals(session_id);

-- 6. Enable realtime for both tables
-- Go to Database → Publications in Supabase Dashboard and enable realtime for:
-- - sessions table
-- - signals table

-- After running this SQL:
-- 1. Go to your Supabase Dashboard
-- 2. Navigate to Database → Publications
-- 3. Under "supabase_realtime", toggle ON for "sessions" table
-- 4. Toggle ON for "signals" table
-- 5. Copy your Supabase URL and anon key from Settings → API

-- Test the setup by inserting a sample session:
-- INSERT INTO sessions (title) VALUES ('Test Event') RETURNING id;
-- INSERT INTO signals (session_id, color) VALUES ('YOUR_SESSION_ID_HERE', 'green');
