function writeBinaryDataVector(fileName, dataVector)

fid = fopen(fileName, 'wb');
fwrite(fid, dataVector, 'single');
fclose(fid);