SELECT
	*,
	concat('pre-', name, '-post')
FROM
	${target}