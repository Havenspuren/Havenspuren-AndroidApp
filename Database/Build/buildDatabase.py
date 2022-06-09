import sqlite3 as sql
import sys
import json
import os
import yaml
import glob


def getFileNumber(path):

    try:
        number = int(os.path.splitext(os.path.basename(path))[0])
    except:
        print(path + "is ignored, because the filename isn't a valid number")
        return -1;
    return number


def getCreateFilePath(args):
    folder = args[1];
    files = glob.glob(folder + "/**/*.json", recursive=True);
    files.sort(key=getFileNumber)
    folder = files[-1]
    print("Used Schema: " + folder)
    return folder


def getInsertFilePath(args):
    return args[2]


def getOutputFilePath(args):
    return args[3]


def getInsertValues(dataObject, toIgnore=[], raw=[]):
    columnString = ""
    valuesString = ""
    for column in dataObject.items():
        key = column[0]
        value = column[1]

        if key not in toIgnore:
            if columnString != "":
                columnString += ","
                valuesString += ","
            columnString += key
            if (type(value) == str or type(value) == bool) and (key not in raw):
                value = "\"" + str(value).replace("\"", "\"\"") + "\""
            else:
                value = str(value)
            valuesString += value
    return "(" + columnString + ") VALUES (" + valuesString + ")"


def getLastIndexOfQuery(tableName):
    return "(SELECT seq from sqlite_sequence where name = \"%s\")" % (tableName)


def getInsertFor(dataObject, tableName, raw=[], toIgnore=[]):
    return "INSERT INTO %s %s ;" % (tableName, getInsertValues(dataObject, toIgnore, raw))


def getInsertQuerrysFromFile(file):
    statements = []
    data = yaml.load(file, Loader=yaml.FullLoader)
    for route in data:
        statements.append(getInsertFor(route, "POIRoute", toIgnore=["waypoints"]))

        for waypoint in route["waypoints"]:
            route_id = getLastIndexOfQuery("POIRoute")
            statements.append(
                getInsertFor({**waypoint, "route_id": route_id}, "POIWaypoint",
                             toIgnore=["pictures", "audio", "trophy"],
                             raw=["route_id"]))

            last_waypoint_id = getLastIndexOfQuery("POIWaypoint");
            last_media_id = getLastIndexOfQuery("Media")
            if "pictures" in waypoint:
                for picture in waypoint["pictures"]:
                    picture["type"] = 2
                    statements.append(getInsertFor(picture, "Media"))
                    statements.append(getInsertFor({"waypoint_id": last_waypoint_id, "media_id": last_media_id},
                                                   "WaypointMediaJunction", raw=["waypoint_id", "media_id"]))

            if "audio" in waypoint:
                audio = waypoint["audio"]
                audio["type"] = 1
                statements.append(getInsertFor(audio, "Media"))
                statements.append(getInsertFor({"waypoint_id": last_waypoint_id, "media_id": last_media_id},
                                               "WaypointMediaJunction", raw=["waypoint_id", "media_id"]))

            if "trophy" in waypoint:
                trophy = waypoint["trophy"]
                trophy["waypoint_id"] = last_waypoint_id
                print(trophy)
                statements.append(getInsertFor(trophy,"Trophy",raw=["waypoint_id"]))

    return statements


if __name__ == "__main__":

    if len(sys.argv) == 4:
        outputFilePath = getOutputFilePath(sys.argv)
        try:
            os.remove(outputFilePath)
        except:
            pass
        conn = sql.connect(outputFilePath)
        createFilePath = getCreateFilePath(sys.argv)
        insertFilePath = getInsertFilePath(sys.argv)

        createFile = open(createFilePath, "r")

        createData = json.load(createFile)
        for entity in createData["database"]["entities"]:
            print(entity["createSql"])
            conn.execute(entity["createSql"].replace("${TABLE_NAME}", entity["tableName"]))

        insertFile = open(insertFilePath, "r", encoding="utf-8")

        for st in getInsertQuerrysFromFile(insertFile):
            print(st)
            conn.execute(st)

        conn.commit()
        conn.close()
        insertFile.close()
        createFile.close()
