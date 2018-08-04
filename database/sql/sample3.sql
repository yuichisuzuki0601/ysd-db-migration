UPDATE hoge SET
	name = name + N'さん'
WHERE
	name = N'佐藤';

UPDATE hoge SET
	name = name + N'です'
WHERE
	name = N'佐藤さん';