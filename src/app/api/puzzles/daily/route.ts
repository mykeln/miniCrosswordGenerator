import { NextResponse } from 'next/server';

const backendUrl = process.env.CROSSWORD_BACKEND_URL ?? 'http://localhost:8080';

export async function POST(request: Request) {
  try {
    const body = await request.json().catch(() => ({}));
    const response = await fetch(`${backendUrl}/api/puzzles/daily`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    if (!response.ok) {
      return NextResponse.json(
        { error: 'Failed to generate daily puzzle' },
        { status: response.status }
      );
    }

    return NextResponse.json(await response.json());
  } catch (error) {
    console.error('Proxy error:', error);
    return NextResponse.json(
      { error: 'Failed to generate daily puzzle' },
      { status: 500 }
    );
  }
}
