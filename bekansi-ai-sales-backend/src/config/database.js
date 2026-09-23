import { createClient } from "@supabase/supabase-js";

const supabaseUrl = process.env.SUPABASE_URL || "https://placeholder-bekansi.supabase.co";
const supabaseKey = process.env.SUPABASE_SERVICE_ROLE_KEY || "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.placeholder";

// If real Supabase credentials are missing, create a mock client with in-memory store so the app can run without crashing
const hasRealCredentials = Boolean(process.env.SUPABASE_URL && process.env.SUPABASE_SERVICE_ROLE_KEY);

function createMockSupabase() {
  const store = {
    customers: [],
    conversations: [],
    messages: [],
    leads: []
  };

  return {
    from: (table) => {
      let queryTable = table;
      let conditions = [];
      let insertData = null;
      let updateData = null;

      const chain = {
        select: () => chain,
        insert: (data) => {
          insertData = data;
          return chain;
        },
        update: (data) => {
          updateData = data;
          return chain;
        },
        eq: (col, val) => {
          conditions.push({ col, val });
          return chain;
        },
        order: () => chain,
        limit: () => chain,
        maybeSingle: async () => {
          const list = store[queryTable] || [];
          const found = list.find((item) =>
            conditions.every((c) => item[c.col] === c.val)
          );
          return { data: found || null, error: null };
        },
        single: async () => {
          if (insertData) {
            const row = {
              id: `mock_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`,
              created_at: new Date().toISOString(),
              ...insertData
            };
            store[queryTable] = store[queryTable] || [];
            store[queryTable].push(row);
            return { data: row, error: null };
          }
          const list = store[queryTable] || [];
          const found = list.find((item) =>
            conditions.every((c) => item[c.col] === c.val)
          );
          return { data: found || null, error: null };
        }
      };

      return chain;
    }
  };
}

export const supabase = hasRealCredentials
  ? createClient(supabaseUrl, supabaseKey, {
      auth: {
        persistSession: false,
        autoRefreshToken: false
      }
    })
  : createMockSupabase();

export default supabase;
