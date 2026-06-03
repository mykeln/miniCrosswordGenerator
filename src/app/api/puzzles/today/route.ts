import { NextResponse } from 'next/server';

const backendUrl = process.env.CROSSWORD_BACKEND_URL ?? 'http://localhost:8080';

export async function GET() {
  try {
    const response = await fetch(`${backendUrl}/api/puzzles/today`, {
      cache: 'no-store',
    });

    if (!response.ok) {
      return NextResponse.json(
        { error: 'Failed to fetch today\'s puzzle' },
        { status: response.status }
      );
    }

    return NextResponse.json(await response.json());
  } catch (error) {
    console.error('Proxy error:', error);
    return NextResponse.json(
      { error: 'Failed to fetch today\'s puzzle' },
      { status: 500 }
    );
  }
}
