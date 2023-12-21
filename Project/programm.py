import cv2
import numpy as np
from matplotlib import pyplot as plt

def find_corners(bound):
    c1 = [bound[3][0], bound[0][1]]
    c2 = [bound[1][0], bound[0][1]]
    c3 = [bound[1][0], bound[2][1]]
    c4 = [bound[3][0], bound[2][1]]
    return [c1, c2, c3, c4]

def find_area(c1):
    return abs(c1[0][0] - c1[1][0]) * abs(c1[0][1] - c1[3][1])

def dist(p1, p2):
    return np.sqrt((p1[0] - p2[0]) ** 2 + (p1[1] - p2[1]) ** 2)

def merge_boxes(c1, c2):
    new_rect = []
    for i in range(4):
        cx = min(c1[i][0], c2[i][0])
        cy = min(c1[i][1], c2[i][1])
        new_rect.append([cx, cy])
    return new_rect

def find_center_coor(c1):
    width = abs(c1[0][0] - c1[1][0])
    height = abs(c1[0][1] - c1[3][1])
    return [c1[0][0] + (width / 2.0), c1[0][1] + (height / 2.0)]

if __name__ == "__main__":
    bndingBx = []
    corners = []

    img = cv2.imread('image1.png', 0)
    blur = cv2.GaussianBlur(img, (5, 5), 0)
    th3 = cv2.adaptiveThreshold(blur, 255, cv2.ADAPTIVE_THRESH_MEAN_C, cv2.THRESH_BINARY, 11, 2)
    th3 = cv2.bitwise_not(th3)

    contours, _ = cv2.findContours(th3, cv2.RETR_CCOMP, cv2.CHAIN_APPROX_SIMPLE)

    for num in range(0, len(contours)):
        if _[0][num][3] == -1:
            left = tuple(contours[num][contours[num][:, :, 0].argmin()][0])
            right = tuple(contours[num][contours[num][:, :, 0].argmax()][0])
            top = tuple(contours[num][contours[num][:, :, 1].argmin()][0])
            bottom = tuple(contours[num][contours[num][:, :, 1].argmax()][0])
            bndingBx.append([top, right, bottom, left])

    for bx in bndingBx:
        corners.append(find_corners(bx))

    imgplot = plt.imshow(img, 'gray')

    plt.clf()
    err = 2
    Area = []

    for corner in corners:
        Area.append(find_area(corner))
    Area = np.asarray(Area)
    avgArea = np.mean(Area)
    stdArea = np.std(Area)
    outlier = (Area < avgArea - stdArea)

    for num in range(0, len(outlier)):
        dot = False
        if outlier[num]:
            black = np.zeros((len(img), len(img[0])), np.uint8)
            cv2.rectangle(black, (corners[num][0][0], corners[num][0][1]), (corners[num][2][0], corners[num][2][1]),
                          (255, 255), -1)
            fin = cv2.bitwise_and(th3, black)
            tempCnt, _ = cv2.findContours(fin, cv2.RETR_CCOMP, cv2.CHAIN_APPROX_SIMPLE)

            for cnt in tempCnt:
                rect = cv2.minAreaRect(cnt)
                axis1 = rect[1][0] / 2.0
                axis2 = rect[1][1] / 2.0
                if axis1 != 0 and axis2 != 0:
                    ratio = axis1 / axis2
                    if 1.0 - err < ratio < err + 1.0:
                        dot = True

            if dot:
                bestCorner = corners[num]
                closest = np.inf
                for crn in corners:
                    width = abs(crn[0][0] - crn[1][0])
                    height = abs(crn[0][1] - crn[3][1])
                    if corners[num][0][1] > crn[0][1]:
                        continue
                    elif dist(corners[num][0], crn[0]) < closest and crn != corners[num]:
                        cent = find_center_coor(crn)
                        bestCorner = crn
                        closest = dist(corners[num][0], crn[0])

                newCorners = merge_boxes(corners[num], bestCorner)
                corners.append(newCorners)
                corners[num] = [[0, 0], [0, 0], [0, 0], [0, 0]]
                bestCorner = [[0, 0], [0, 0], [0, 0], [0, 0]]

    for bx in corners:
        width = abs(bx[1][0] - bx[0][0])
        height = abs(bx[3][1] - bx[0][1])
        if width * height == 0:
            continue
        plt.plot([bx[0][0], bx[1][0]], [bx[0][1], bx[1][1]], 'g-', linewidth=2)
        plt.plot([bx[1][0], bx[2][0]], [bx[1][1], bx[2][1]], 'g-', linewidth=2)
        plt.plot([bx[2][0], bx[3][0]], [bx[2][1], bx[3][1]], 'g-', linewidth=2)
        plt.plot([bx[3][0], bx[0][0]], [bx[3][1], bx[0][1]], 'g-', linewidth=2)

    plt.imshow(th3, 'gray')
    plt.show()
