// Supabase Edge Function: proxies requests to NewsData.io so the real API
// key never ships inside the Android app.
//
// Deploy:
//   supabase functions deploy news-proxy --no-verify-jwt
// Set the secret once (never committed anywhere):
//   supabase secrets set NEWSDATA_API_KEY=your_real_key_here
//
// The Android app calls this function's URL instead of newsdata.io
// directly, forwarding only non-secret query params (category, q,
// language). This function attaches the real key and forwards the request.

const NEWSDATA_BASE_URL = "https://newsdata.io/api/1/latest";

// Native Android clients don't need CORS, but these headers are harmless
// and let you test the function from a browser or curl during setup.
const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  const apiKey = Deno.env.get("NEWSDATA_API_KEY");
  if (!apiKey) {
    return new Response(
      JSON.stringify({
        status: "error",
        results_message: "Server misconfigured: NEWSDATA_API_KEY secret is not set on this function.",
      }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } },
    );
  }

  const incomingUrl = new URL(req.url);
  const forwardParams = new URLSearchParams();
  forwardParams.set("apikey", apiKey);

  // Only forward the params we actually expect — never let the client
  // pass through arbitrary query params to the upstream API.
  for (const key of ["category", "q", "language", "image"]) {
    const value = incomingUrl.searchParams.get(key);
    if (value) forwardParams.set(key, value);
  }

  const targetUrl = `${NEWSDATA_BASE_URL}?${forwardParams.toString()}`;

  try {
    const upstream = await fetch(targetUrl);
    const body = await upstream.text();
    return new Response(body, {
      status: upstream.status,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  } catch (error) {
    return new Response(
      JSON.stringify({
        status: "error",
        results_message: `Proxy error reaching NewsData.io: ${error instanceof Error ? error.message : "unknown"}`,
      }),
      { status: 502, headers: { ...corsHeaders, "Content-Type": "application/json" } },
    );
  }
});
