SELECT tt.name AS track_type, COUNT(*) AS num_segments
FROM Line_Segment ls
JOIN Line l ON l.id = ls.line
JOIN TrackType tt ON tt.id = ls.track_type
WHERE l.owner = 'PT503933813'
GROUP BY tt.name;