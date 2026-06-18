package com.mahoganyhomeshighlighter;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum StatusTextPosition
{
	ABOVE("Above"),
	ON("On"),
	BELOW("Below");

	private final String label;

	@Override
	public String toString()
	{
		return label;
	}
}
